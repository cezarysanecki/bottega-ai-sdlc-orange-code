package com.clinictraining.medbackend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(
                java.util.List.of(
                        new CaffeineCache("providerSchedule", Caffeine.newBuilder()
                                .expireAfterWrite(Duration.ofMinutes(30))
                                .maximumSize(100)
                                .build()),
                        new CaffeineCache("patientSchedule", Caffeine.newBuilder()
                                .expireAfterWrite(Duration.ofSeconds(5)) // BUG: Should be longer duration
                                .maximumSize(2)
                                .build())
                )
        );
        return cacheManager;
    }
}
