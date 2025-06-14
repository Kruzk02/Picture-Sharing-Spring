package com.app.Service.cache;

import com.app.Model.Media;
import com.app.Service.MediaService;
import com.app.exception.sub.MediaNotFoundException;
import com.app.helper.CachedServiceHelper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Primary
public class CachedMediaService extends CachedServiceHelper<Media> implements MediaService {

    private final MediaService mediaService;

    protected CachedMediaService(RedisTemplate<String, Media> mediaRedisTemplate, @Qualifier("mediaServiceImpl") MediaService mediaService) {
        super(mediaRedisTemplate);
        this.mediaService = mediaService;
    }

    @Override
    public Media findById(Long id) {
        var cached = super.getOrLoad("media:" + id, () -> mediaService.findById(id), Duration.ofHours(2));
        return cached.orElseThrow(() -> new MediaNotFoundException("Media not found with a id: " + id));
    }
}
