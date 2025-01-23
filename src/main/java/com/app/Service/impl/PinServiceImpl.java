package com.app.Service.impl;

import com.app.DAO.MediaDao;
import com.app.DAO.PinDao;
import com.app.DAO.UserDao;
import com.app.DTO.request.PinRequest;
import com.app.Model.Media;
import com.app.Model.MediaType;
import com.app.Model.Pin;
import com.app.Model.User;
import com.app.Service.PinService;
import com.app.exception.sub.PinIsEmptyException;
import com.app.exception.sub.PinNotFoundException;
import com.app.exception.sub.UserNotMatchException;
import com.app.utils.FileUtils;
import com.app.utils.MediaUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
    private final MediaUtils mediaUtils;
    private final FileUtils fileUtils;
    private final RedisTemplate<Object,Object> redisTemplate;

    /**
     * Retrieves all pins.
     *
     * @return A List of all pins.
     */
    public List<Pin> getAllPins(int offset){
        List<Pin> pins = pinDao.getAllPins(offset);
        List<Object> cacheKeys = pins.stream()
                .map(pin -> "pins:" + pin.getId())
                .collect(Collectors.toList());
        List<Object> cachePins = redisTemplate.opsForValue().multiGet(cacheKeys);

        for (int i = 0; i < Objects.requireNonNull(cachePins).size(); i++) {
            if (cachePins.get(i) == null) {
                redisTemplate.opsForValue().set("pins:" + pins.get(i).getId(),pins.get(i),Duration.ofHours(2));
            }
        }
        return pins;
    }

    @Override
    public Pin save(PinRequest pinRequest) {
        if (pinRequest.file().isEmpty()) {
            throw new PinIsEmptyException("A pin must have file");
        }

        String filename = mediaUtils.generateUniqueFilename(pinRequest.file().getOriginalFilename());
        String extension = mediaUtils.getFileExtension(pinRequest.file().getOriginalFilename());

        fileUtils.save(pinRequest.file(), filename, extension);
        Media media = mediaDao.save(Media.builder()
                .url(filename)
                .mediaType(MediaType.fromExtension(extension))
                .build());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Pin pin = Pin.builder()
                .description(pinRequest.description())
                .userId(userDao.findUserByUsername(authentication.getName()).getId())
                .mediaId(media.getId())
                .build();
        return pinDao.save(pin);
    }

    @Override
    public Pin update(Long id, PinRequest pinRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Pin existingPin = pinDao.findFullById(id);

        if (existingPin == null) {
            throw new PinNotFoundException("Pin not found with a id: " + id);
        }

        User user = userDao.findUserByUsername(authentication.getName());

        if (user == null || !Objects.equals(user.getId(), existingPin.getUserId())) {
            throw new UserNotMatchException("User does not matching with a pin owner");
        }

        if (pinRequest.file() != null && !pinRequest.file().isEmpty()) {
            Media existingMedia = mediaDao.findById(existingPin.getMediaId());
            String extensionOfExistingMedia = mediaUtils.getFileExtension(existingMedia.getUrl());

            String filename = mediaUtils.generateUniqueFilename(pinRequest.file().getOriginalFilename());
            String extension = mediaUtils.getFileExtension(pinRequest.file().getOriginalFilename());

            CompletableFuture.runAsync(() -> fileUtils.delete(existingMedia.getUrl(), extensionOfExistingMedia))
                    .thenRunAsync(() -> fileUtils.save(pinRequest.file(), filename, extension));

            mediaDao.update(existingPin.getMediaId(), Media.builder()
                    .url(filename)
                    .mediaType(MediaType.fromExtension(extension))
                    .build());
        }

        existingPin.setDescription(pinRequest.description() != null? pinRequest.description() : existingPin.getDescription());

        return pinDao.update(id, existingPin);
    }

    @Override
    public Pin findBasicById(Long id) {
        Pin cachedPin = (Pin) redisTemplate.opsForValue().get("pin_basic:" + id);
        if (cachedPin != null) {
            return cachedPin;
        }
        Pin pin = pinDao.findBasicById(id);
        if (pin != null) {
            redisTemplate.opsForValue().set("pin_basic: " + id, pin, Duration.ofHours(2));
        }
        return pin;
    }

    @Override
    public Pin findFullById(Long id) {
        Pin cachedPin = (Pin) redisTemplate.opsForValue().get("pin_full:" + id);
        if (cachedPin != null) {
            return cachedPin;
        }

        Pin pin = pinDao.findFullById(id);
        if (pin != null) {
            redisTemplate.opsForValue().set("pin_full:" + id, pin, Duration.ofHours(2));
        }
        return pin;
    }

    @Override
    public List<Pin> findNewestPin(int limit, int offset) {
        List<Pin> pins = pinDao.findNewestPin(limit, offset);
        List<Object> cacheKeys = pins.stream()
                .map(pin -> "pins:" + pin.getId())
                .collect(Collectors.toList());
        List<Object> cachePin = redisTemplate.opsForValue().multiGet(cacheKeys);

        for (int i = 0; i < cachePin.size(); i++) {
            if (cachePin.get(i) == null) {
                redisTemplate.opsForValue().set("pins:" + pins.get(i).getId(), pins.get(i), Duration.ofHours(2));
            }
        }
        return pins;
    }

    @Override
    public List<Pin> findOldestPin(int limit, int offset) {
        List<Pin> pins = pinDao.findOldestPin(limit, offset);
        List<Object> cacheKeys = pins.stream()
                .map(pin -> "pins:" + pin.getId())
                .collect(Collectors.toList());
        List<Object> cachePin = redisTemplate.opsForValue().multiGet(cacheKeys);

        for (int i = 0; i < cachePin.size(); i++) {
            if (cachePin.get(i) == null) {
                redisTemplate.opsForValue().set("pins:" + pins.get(i).getId(), pins.get(i), Duration.ofHours(2));
            }
        }
        return pins;
    }

    @Override
    public List<Pin> findPinByUserId(Long userId, int limit, int offset) {
        List<Pin> pins = pinDao.findPinByUserId(userId,limit, offset);
        List<Object> cacheKeys = pins.stream()
                .map(pin -> "pins_user:" + pin.getId())
                .collect(Collectors.toList());
        List<Object> cachePin = redisTemplate.opsForValue().multiGet(cacheKeys);

        for (int i = 0;i < cachePin.size();i++) {
            if (cachePin.get(i) == null) {
                redisTemplate.opsForValue().set("pins_user:" + pins.get(i).getId(), pins.get(i), Duration.ofHours(2));
            }
        }
        return pins;
    }

    @Override
    public void delete(Long id) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userDao.findUserByUsername(authentication.getName());

        Pin pin = pinDao.findBasicById(id);
        if (pin == null) {
            throw new PinNotFoundException("Pin not found with a id: " + id);
        }

        if (!Objects.equals(user.getId(), pin.getUserId())) {
            throw new UserNotMatchException("Authenticated user does not own the pin");
        }

        Media media = mediaDao.findById(pin.getMediaId());
        fileUtils.delete(media.getUrl(), media.getMediaType().toString());
        mediaDao.deleteById(media.getId());

        redisTemplate.opsForValue().getAndDelete("pin_basic:" + id);
        redisTemplate.opsForValue().getAndDelete("pin_full:" + id);
        redisTemplate.opsForValue().getAndDelete("pins:" + id);
        pinDao.deleteById(id);
    }
}
