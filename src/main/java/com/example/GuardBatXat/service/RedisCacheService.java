package com.example.GuardBatXat.service;

public interface RedisCacheService {
    void setCache(String key, Object value, long timeoutInMinutes);
    Object getCache(String key);
    Object getAndDelete(String key);
    void deleteCache(String key);
}
