package com.app.Service.impl;

import com.app.DAO.MediaDao;
import com.app.Model.Media;
import com.app.Service.MediaService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Log4j2
@AllArgsConstructor
@Service
public class MediaServiceImpl implements MediaService {

    private final MediaDao mediaDao;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Media findById(Long id) {
        Media cacheMedia = (Media) redisTemplate.opsForValue().get("media: " + id);
        if (cacheMedia != null && cacheMedia.getUrl() != null) {
            log.info("Cache hit for media with id {}", id);
            return cacheMedia;
        }
        log.info("Cache miss for media with id {}", id);
        Media media = mediaDao.findById(id);
        if (media != null) {
            redisTemplate.opsForValue().set("media: " + id, media, Duration.ofHours(1));
        }
        return media;
    }

    @Override
    public Media findByCommentId(Long commentId) {
        Media cacheMedia = (Media) redisTemplate.opsForValue().get("media_commentId: " + commentId);
        if (cacheMedia != null && cacheMedia.getUrl() != null) {
            log.info("Cache hit for media with comment id {}", commentId);
            return cacheMedia;
        }
        log.info("Cache miss for media with comment id {}", commentId);
        Media media = mediaDao.findByCommentId(commentId);
        if (media != null) {
            redisTemplate.opsForValue().set("media_commentId: " + commentId, media, Duration.ofHours(1));
        }
        return media;
    }
}
