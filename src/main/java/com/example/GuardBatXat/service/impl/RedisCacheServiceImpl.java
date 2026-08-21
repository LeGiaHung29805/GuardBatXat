package com.example.GuardBatXat.service.impl;

import com.example.GuardBatXat.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisCacheServiceImpl implements RedisCacheService {

    private static final DefaultRedisScript<Object> GET_AND_DELETE_SCRIPT =
            new DefaultRedisScript<>(
                    "local value = redis.call('GET', KEYS[1]); " +
                            "if value then redis.call('DEL', KEYS[1]); end; " +
                            "return value;",
                    Object.class
            );

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void setCache(String key, Object value, long timeoutInMinutes) {
        redisTemplate.opsForValue().set(key, value, timeoutInMinutes, TimeUnit.MINUTES);
    }

    @Override
    public Object getCache(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public Object getAndDelete(String key) {
        // Redis GETDEL chỉ có từ 6.2. Lua giữ thao tác nguyên tử và tương thích
        // với các bản Redis cũ đang được dùng trên máy triển khai.
        return redisTemplate.execute(
                GET_AND_DELETE_SCRIPT,
                Collections.singletonList(key)
        );
    }

    @Override
    public void deleteCache(String key) {
        redisTemplate.delete(key);
    }
}
