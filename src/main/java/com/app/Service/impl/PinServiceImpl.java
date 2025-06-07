package com.app.Service.impl;

import com.app.DAO.HashtagDao;
import com.app.DAO.MediaDao;
import com.app.DAO.PinDao;
import com.app.DAO.UserDao;
import com.app.DTO.request.PinRequest;
import com.app.Model.*;
import com.app.Service.PinService;
import com.app.exception.sub.PinIsEmptyException;
import com.app.exception.sub.PinNotFoundException;
import com.app.exception.sub.UserNotMatchException;
import com.app.storage.FileManager;
import com.app.storage.MediaManager;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Pin Service class responsible for handling operations relates to Pins.<p>
 * This class interacts with the PinDaoImpl for data access,
 * and utilizes ModelMapper for mapping between DTOs and entity objects.
 */
@Service
@AllArgsConstructor
public class PinServiceImpl implements PinService {

    private final PinDao pinDao;
    private final UserDao userDao;
    private final MediaDao mediaDao;
    private final HashtagDao hashtagDao;
    private final RedisTemplate<String,Pin> pinRedisTemplate;

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userDao.findUserByUsername(authentication.getName());
    }
    /**
     * Retrieves all pins.
     *
     * @return A List of all pins.
     */
    public List<Pin> getAllPins(SortType sortType, int limit, int offset) {
        String redisKey = "pins:" + sortType;

        List<Pin> cachedPin = pinRedisTemplate.opsForList().range(redisKey,offset, offset - limit + 1);
        if (cachedPin != null && !cachedPin.isEmpty()) {
            return cachedPin;
        }

        List<Pin> pins = pinDao.getAllPins(sortType, limit, offset);
        if (pins.isEmpty()) {
            return Collections.emptyList();
        }

        pinRedisTemplate.opsForList().rightPushAll(redisKey, pins);
        pinRedisTemplate.expire(redisKey,Duration.ofHours(2));

        return pins;
    }

    @Override
    public List<Pin> getAllPinsByHashtag(String tag, int limit, int offset) {
        String redisKey = "pins_hashtag: " + tag;

        List<Pin> cachedPin = pinRedisTemplate.opsForList().range(redisKey, offset, offset - limit + 1);
        if (cachedPin != null && !cachedPin.isEmpty()) {
            return cachedPin;
        }

        List<Pin> pins = pinDao.getAllPinsByHashtag(tag, limit, offset);
        if (pins.isEmpty()) {
            return Collections.emptyList();
        }

        pinRedisTemplate.opsForList().rightPushAll(redisKey, pins);
        pinRedisTemplate.expire(redisKey, Duration.ofHours(2));

        return pins;
    }

    @Transactional
    @Override
    public Pin save(PinRequest pinRequest) {
        if (pinRequest.file().isEmpty()) {
            throw new PinIsEmptyException("A pin must have file");
        }

        Set<String> tagsToFind = pinRequest.hashtags();
        Map<String, Hashtag> tags = hashtagDao.findByTag(tagsToFind);

        List<Hashtag> hashtags = new ArrayList<>();
        for (String tag : tagsToFind) {
            Hashtag hashtag = tags.get(tag);
            if (hashtag == null) {
                hashtag = hashtagDao.save(Hashtag.builder().tag(tag).build());
            }
            hashtags.add(hashtag);
        }

        String filename = MediaManager.generateUniqueFilename(pinRequest.file().getOriginalFilename());
        String extension = MediaManager.getFileExtension(pinRequest.file().getOriginalFilename());

        FileManager.save(pinRequest.file(), filename, extension);
        Media media = mediaDao.save(Media.builder()
                .url(filename)
                .mediaType(MediaType.fromExtension(extension))
                .build());

        Pin pin = Pin.builder()
                .description(pinRequest.description())
                .userId(getAuthenticatedUser().getId())
                .mediaId(media.getId())
                .hashtags(hashtags)
                .build();
        return pinDao.save(pin);
    }

    @Override
    public Pin update(Long id, PinRequest pinRequest) {

        Pin existingPin = pinDao.findById(id, false);

        if (existingPin == null) {
            throw new PinNotFoundException("Pin not found with a id: " + id);
        }

        if (getAuthenticatedUser() == null || !Objects.equals(getAuthenticatedUser().getId(), existingPin.getUserId())) {
            throw new UserNotMatchException("User does not matching with a pin owner");
        }

        pinRedisTemplate.delete("pin:" + id + ":basic");
        pinRedisTemplate.delete("pin:" + id + ":details");

        if (pinRequest.file() != null && !pinRequest.file().isEmpty()) {
            Media existingMedia = mediaDao.findById(existingPin.getMediaId());
            String extensionOfExistingMedia = MediaManager.getFileExtension(existingMedia.getUrl());

            String filename = MediaManager.generateUniqueFilename(pinRequest.file().getOriginalFilename());
            String extension = MediaManager.getFileExtension(pinRequest.file().getOriginalFilename());

            CompletableFuture.runAsync(() -> FileManager.delete(existingMedia.getUrl(), extensionOfExistingMedia))
                    .thenRunAsync(() -> FileManager.save(pinRequest.file(), filename, extension));

            mediaDao.update(existingPin.getMediaId(), Media.builder()
                    .url(filename)
                    .mediaType(MediaType.fromExtension(extension))
                    .build());
        }

        Set<String> tagsToFind = pinRequest.hashtags();
        Map<String, Hashtag> tags = hashtagDao.findByTag(tagsToFind);

        List<Hashtag> hashtags = new ArrayList<>();
        for (String tag : tagsToFind) {
            Hashtag hashtag = tags.get(tag);
            if (hashtag == null) {
                hashtag = hashtagDao.save(Hashtag.builder().tag(tag).build());
            }
            hashtags.add(hashtag);
        }

        existingPin.setDescription(pinRequest.description() != null? pinRequest.description() : existingPin.getDescription());
        existingPin.setHashtags(hashtags);
        return pinDao.update(id, existingPin);
    }

    /**
     * Retrieves a pin with little or full details, using database or cache
     * @param id The id of the pin to be found.
     * @param fetchDetails The details of little or full details of the pin
     * @return A pin with specified id, either fetch from database or cache. If no pin are found, an exception is thrown
     */
    @Override
    public Pin findById(Long id, boolean fetchDetails) {
        String cacheKey = fetchDetails ? "pin:" + id + ":details" : "pin:" + id + ":basic";

        // Retrieved cache pin from redis
        Pin cachedPin = pinRedisTemplate.opsForValue().get(cacheKey);

        if (cachedPin != null) {
            // Return cache pin if found
            return cachedPin;
        }

        // Fetch pin from the database and handle null safely
        Pin pin = Optional.ofNullable(pinDao.findById(id, fetchDetails))
                .orElseThrow(() -> new PinNotFoundException("Pin not found with a id: " + id));

        // Store in cache for 2 hours
        pinRedisTemplate.opsForValue().set(cacheKey, pin, Duration.ofHours(2));

        return pin;
    }

    /**
     * Retrieves a list of pin associated with specific user, using both database and cache.
     * @param userId The id of the user whose pin are to be retrieved.
     * @param limit The maximum number of pin to be return.
     * @param offset The offset to paginate the pin result.
     * @return A list of pin associated with specified user ID, either from fetch database or cache. If no pin are found, an empty list is returned
     */
    @Override
    public List<Pin> findPinByUserId(Long userId, int limit, int offset) {
        String redisKey = "pins:user:" + userId;

        List<Pin> cachedPin = pinRedisTemplate.opsForList().range(redisKey,offset, offset + limit - 1);
        if (cachedPin != null && !cachedPin.isEmpty()) {
            return cachedPin;
        }

        List<Pin> pins = pinDao.findPinByUserId(userId, limit, offset);
        if (pins.isEmpty()) {
            return Collections.emptyList();
        }

        pinRedisTemplate.opsForList().rightPushAll(redisKey, pins);
        pinRedisTemplate.expire(redisKey,Duration.ofHours(2));

        return pins;
    }

    @Override
    public void delete(Long id) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userDao.findUserByUsername(authentication.getName());

        Pin pin = pinDao.findById(id, false);
        if (pin == null) {
            throw new PinNotFoundException("Pin not found with a id: " + id);
        }

        if (!Objects.equals(user.getId(), pin.getUserId())) {
            throw new UserNotMatchException("Authenticated user does not own the pin");
        }

        Media media = mediaDao.findById(pin.getMediaId());
        FileManager.delete(media.getUrl(), media.getMediaType().toString());
        mediaDao.deleteById(media.getId());

        pinRedisTemplate.delete("pin:" + id + ":basic");
        pinRedisTemplate.delete("pin:" + id + ":details");
        pinRedisTemplate.delete("pins:" + SortType.NEWEST);
        pinRedisTemplate.delete("pins:" + SortType.OLDEST);
        pinRedisTemplate.delete("pins:user:" + pin.getUserId());
        pinDao.deleteById(id);
    }
}
