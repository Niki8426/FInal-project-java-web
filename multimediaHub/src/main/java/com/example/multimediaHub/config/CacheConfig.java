package com.example.multimediaHub.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    // Този метод вдига в паметта мениджър за кеширане на данни.
    // Създаваме си кеш с име "mediaCache", където ще държим по-тежките неща (като медийния каталог),
    // за да не се налага Spring-ът да ходи и да разпитва базата данни при всяко цъкане на потребителя.
    @Bean
    public CacheManager cacheManager() {
        // Регистрирам името на кеша тук
        return new ConcurrentMapCacheManager("mediaCache");
    }
}