package com.app.Service.cache;

import com.app.DTO.request.PinRequest;
import com.app.Model.Pin;
import com.app.Model.SortType;
import com.app.Service.PinService;
import com.app.exception.sub.PinNotFoundException;
import com.app.helper.CachedServiceHelper;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Primary
public class CachedPinService extends CachedServiceHelper<Pin> implements PinService {

  private final PinService delegate;

  public CachedPinService(
      @Qualifier("pinServiceImpl") PinService pinServiceImpl,
      RedisTemplate<String, Pin> pinRedisTemplate) {
    super(pinRedisTemplate);
    this.delegate = pinServiceImpl;
  }

  @Override
  public List<Pin> getAllPins(SortType sortType, int limit, int offset) {
    var redisKey = "pins:" + sortType + ":limit:" + limit + ":offset:" + offset;
    return super.getListOrLoad(
        redisKey,
        () -> delegate.getAllPins(sortType, limit, offset),
        limit,
        offset,
        Duration.ofHours(2));
  }

  @Override
  public List<Pin> getAllPinsByHashtag(String tag, int limit, int offset) {
    var redisKey = "pins_hashtag:" + tag + ":limit:" + limit + ":offset:" + offset;
    return super.getListOrLoad(
        redisKey,
        () -> delegate.getAllPinsByHashtag(tag, limit, offset),
        limit,
        offset,
        Duration.ofHours(2));
  }

  @Override
  public Pin save(PinRequest pinRequest) {
    var pin = delegate.save(pinRequest);
    var cached = super.getOrLoad("pin:" + pin.getId() + ":details", () -> pin, Duration.ofHours(2));
    return cached.orElse(pin);
  }

  @Override
  public Pin update(Long id, PinRequest pinRequest) {
    var pin = delegate.update(id, pinRequest);
    super.delete("pin:" + id + ":details");
    var cached = super.getOrLoad("pin:" + pin.getId() + ":details", () -> pin, Duration.ofHours(2));
    return cached.orElse(pin);
  }

  @Override
  public Pin findById(Long id, boolean fetchDetails) {
    var cacheKey = fetchDetails ? "pin:" + id + ":details" : "pin:" + id + ":basic";
    var cached =
        super.getOrLoad(cacheKey, () -> delegate.findById(id, fetchDetails), Duration.ofHours(2));
    return cached.orElseThrow(() -> new PinNotFoundException("Pin not found with a id: " + id));
  }

  @Override
  public List<Pin> findPinByUserId(Long userId, int limit, int offset) {
    var redisKey = "user:" + userId + ":pins";
    return super.getListOrLoad(
        redisKey,
        () -> delegate.findPinByUserId(userId, limit, offset),
        limit,
        offset,
        Duration.ofHours(2));
  }

  @Override
  public void delete(Long id) throws IOException {
    var pin = delegate.findById(id, false);
    super.delete("pin:*");
    super.delete("user:" + pin.getUserId() + ":pins");
  }
}
