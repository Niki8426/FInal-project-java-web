package com.example.multimediaHub.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.junit.jupiter.api.Assertions.*;

class CacheConfigTest {

    @Test
    void testCacheManagerBeanCreation() {
        // Arrange
        CacheConfig cacheConfig = new CacheConfig();

        // Act
        CacheManager cacheManager = cacheConfig.cacheManager();

        // Assert
        assertNotNull(cacheManager, "CacheManager бинът не трябва да е null");
        assertTrue(cacheManager instanceof ConcurrentMapCacheManager, "Трябва да е инстанция на ConcurrentMapCacheManager");

        // Проверяваме дали кешът с име "mediaCache" е регистриран
        assertNotNull(cacheManager.getCache("mediaCache"), "Кешът 'mediaCache' трябва да е наличен");
    }
}