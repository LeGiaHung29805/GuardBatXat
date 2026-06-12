package com.example.GuardBatXat.service.impl;

import com.example.GuardBatXat.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisCacheServiceImpl implements RedisCacheService {

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
    public void deleteCache(String key) {
        redisTemplate.delete(key);
    }
}
