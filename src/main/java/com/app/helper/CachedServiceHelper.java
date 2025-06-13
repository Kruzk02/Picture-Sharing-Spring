package com.app.helper;

import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class CachedServiceHelper<T> {

    protected final RedisTemplate<String, T> redisTemplate;

    protected CachedServiceHelper(RedisTemplate<String, T> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // --- Value Operations ---
    protected Optional<T> get(String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    protected void set(String key, T value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    protected boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    protected Optional<T> getOrLoad(String key, Supplier<T> supplier, Duration ttl) {
        Optional<T> value = get(key);
        if (value.isEmpty()) {
            value = Optional.ofNullable(supplier.get());
            value.ifPresent(t -> redisTemplate.opsForValue().set(key, t, ttl));
        }
        return value;
    }

    // --- List Operations ---

    protected void pushToList(String key, List<T> value, Duration ttl) {
        redisTemplate.opsForList().rightPushAll(key, value);
        redisTemplate.expire(key, ttl);
    }

    protected List<T> getListRange(String key, int limit, int offset) {
        return redisTemplate.opsForList().range(key, offset, offset + limit - 1);
    }

    protected T popFromList(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    protected List<T> getListOrLoad(String key, Supplier<List<T>> suppliers, int limit, int offset, Duration ttl) {
        List<T> value = getListRange(key, limit, offset);
        if (value.isEmpty()) {
            value = suppliers.get();
            if (!value.isEmpty()) {
                pushToList(key, value, ttl);
            }
        }
        return value;
    }
}
