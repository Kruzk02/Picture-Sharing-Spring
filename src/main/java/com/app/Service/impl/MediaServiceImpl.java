package com.app.Service.impl;

import com.app.DAO.MediaDao;
import com.app.DTO.request.CreateMediaRequest;
import com.app.DTO.request.UpdatedMediaRequest;
import com.app.Model.Media;
import com.app.Model.MediaType;
import com.app.Service.MediaService;
import com.app.exception.sub.MediaNotFoundException;
import com.app.exception.sub.MediaNotSupportException;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
@Service
public class MediaServiceImpl implements MediaService {

    private final MediaDao mediaDao;

    @Override
    public Media save(CreateMediaRequest request) {
         String originFilename = request.file().getOriginalFilename();
         if (originFilename == null) {
             throw new IllegalArgumentException("Filename cannot be null");
         }

         if (!MediaType.IMAGE.isFormatValid(originFilename) && !MediaType.VIDEO.isFormatValid(originFilename)) {
             throw new MediaNotSupportException("Media format not supported");
         }

         int lastDotOfIndex = originFilename.lastIndexOf(".");
         String extension = "";

         if (lastDotOfIndex != 1){
             extension = originFilename.substring(lastDotOfIndex).replaceAll("[(){}]", "");
         }
         String filename = UUID.fromString(originFilename + LocalDateTime.now()) + extension;
         filename = filename + extension.replaceAll("[(){}]", "");

         MediaType mediaType = MediaType.getByExtension(extension);
         if (mediaType == null) {
             throw new MediaNotSupportException("Media not supported");
         }

        return Media
                .builder()
                .url(filename)
                .mediaType(mediaType)
                .build();
    }

    @Override
    public Media update(Long id, UpdatedMediaRequest request) {
        Media existingMedia = mediaDao.findById(id);
        if (existingMedia == null) {
            throw new MediaNotFoundException("Media not found with a id: " + id);
        }

        String originFilename = request.file().getOriginalFilename();

        if (originFilename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }

        if (!MediaType.IMAGE.isFormatValid(originFilename) && !MediaType.VIDEO.isFormatValid(originFilename)) {
            throw new MediaNotSupportException("Media format not supported");
        }

        int lastDotOfIndex = originFilename.lastIndexOf(".");
        String extension = "";

        if (lastDotOfIndex != 1) {
            extension = originFilename.substring(lastDotOfIndex).replaceAll("[(){}]", "");
        }

        String filename = UUID.fromString(originFilename + LocalDateTime.now()) + extension;
        filename = filename + extension.replaceAll("[(){}]", "");

        MediaType mediaType = MediaType.getByExtension(extension);
        if (mediaType == null) {
            throw new MediaNotSupportException("Unsupported media type");
        }

        return Media
                .builder()
                .id(existingMedia.getId())
                .url(filename)
                .mediaType(mediaType)
                .created_at(existingMedia.getCreated_at())
                .build();
    }

    @Override
    public Media findById(Long id) {
        return mediaDao.findById(id);
    }

    @Override
    public Media findByCommentId(Long commentId) {
        return mediaDao.findByCommentId(commentId);
    }

    @Override
    public void deleteById(Long id) {
        Media existingMedia = mediaDao.findById(id);
        if (existingMedia != null) {
            mediaDao.deleteById(id);
        }
    }
}
