package com.app.Service.impl;

import com.app.DAO.MediaDao;
import com.app.Model.Media;
import com.app.Service.MediaService;
import com.app.exception.sub.MediaNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Log4j2
@AllArgsConstructor
@Service
@Qualifier("mediaServiceImpl")
public class MediaServiceImpl implements MediaService {

    private final MediaDao mediaDao;

    @Override
    public Media findById(Long id) {
        Media media = mediaDao.findById(id);
        if (media == null) {
            throw new MediaNotFoundException("Media not found with a id: " + id);
        }
        return media;
    }
}
