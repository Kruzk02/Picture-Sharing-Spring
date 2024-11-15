package com.app.Service;

import com.app.DAO.CommentDao;
import com.app.DAO.PinDao;
import com.app.DTO.request.UploadPinRequest;
import com.app.Model.Comment;
import com.app.Model.Pin;
import com.app.Model.User;
import com.app.exception.sub.UserNotMatchException;
import org.apache.commons.lang3.RandomStringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
public class PinService {

    private final PinDao pinDao;
    private final CommentDao commentDao;
    private final RedisTemplate<Object,Object> redisTemplate;

    /**
     * Constructs a new PinService.
     *
     * @param pinDao The PinDaoImpl for accessing pin related data.
     */
    @Autowired
    public PinService(PinDao pinDao, CommentDao commentDao, RedisTemplate<Object, Object> redisTemplate) {
        this.pinDao = pinDao;
        this.commentDao = commentDao;
        this.redisTemplate = redisTemplate;
    }

    @Async
    public CompletableFuture<Pin> asyncData(Long userId, UploadPinRequest request, MultipartFile multipartFile) throws IOException {
        return CompletableFuture.completedFuture(save(userId, request, multipartFile));
    }
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

    public List<Comment> getAllCommentByPinId(Long pinId){
        List<Comment> comments = commentDao.findByPinId(pinId);
        List<Object> cacheKeys = comments.stream()
                .map(comment -> "comments:" + comment.getId())
                .collect(Collectors.toList());
        List<Object> cacheComment = redisTemplate.opsForValue().multiGet(cacheKeys);

        for (int i = 0;i < Objects.requireNonNull(cacheComment).size();i++) {
            if (cacheComment.get(i) == null) {
                redisTemplate.opsForValue().set("comments:" + comments.get(i).getId(),comments.get(i),Duration.ofHours(2));
            }
        }
        return comments;
    }

    /**
     * Saves a new pin with a provided PinDTO and MultipartFile. <p>
     * Saves the upload file to the "upload" directory and sets the image URL and file name to pin.
     *
     * @param request The UploadPinRequest object containing pin information.
     * @param multipartFile The MultipartFile containing the upload image file.
     * @return The saved Pin entity.
     * @throws IOException If an I/O error occurs while saving the file.
     */
    public Pin save(Long userId, UploadPinRequest request, MultipartFile multipartFile) throws IOException {
        Path uploadPath = Paths.get("upload");
        if(!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        int lastIndexOfDot = Objects.requireNonNull(multipartFile.getOriginalFilename()).lastIndexOf('.');
        String extension = multipartFile.getOriginalFilename().substring(lastIndexOfDot);

        String fileCode = RandomStringUtils.randomAlphabetic(8) + extension;
        Path filePath = uploadPath.resolve(fileCode);

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Could not save file: " + fileCode, e);
        }
        Pin pin = Pin.builder()
                .userId(userId)
                .image_url(filePath.toString())
                .fileName(fileCode)
                .description(request.description())
                .build();
        return pinDao.save(pin);
    }

    /**
     * Retrieves a pin by its ID.
     *
     * @param id The ID of the pin to retrieve.
     * @return The Pin entity corresponding to the provided ID.
     */

    public Pin findById(Long id){
        Pin cachePin = (Pin) redisTemplate.opsForValue().get("pins:" + id);
        if (cachePin != null && cachePin.getFileName() != null && cachePin.getDescription() != null)
            return cachePin;

        Pin pin = pinDao.findById(id);
        if (pin != null) {
            redisTemplate.opsForValue().set("pin:" + id,pin,Duration.ofHours(2));
        }
        return pin;
    }

    /**
     * Deletes a Pin by its ID if user id match with pin.
     *
     * @param id The ID of the pin to delete.
     */
    public void deleteIfUserMatches(User user,Long id) throws IOException {
        Pin pin = pinDao.findUserIdByPinId(id);
        if(pin != null && Objects.equals(user.getId(),pin.getUserId())){
            Path path = Path.of(pin.getImage_url());
            if (Files.exists(path)) Files.delete(path);
            else throw new FileNotFoundException("File not found with a pin id: " + pin.getId());

            redisTemplate.opsForValue().getAndDelete("pin:" + id);
            redisTemplate.opsForValue().getAndDelete("pins:" + id);
            pinDao.deleteById(id);
        } else if (pin != null && !Objects.equals(user.getId(), pin.getUserId())) {
            throw new UserNotMatchException("User does not match with a pin owner");
        }
    }
}
