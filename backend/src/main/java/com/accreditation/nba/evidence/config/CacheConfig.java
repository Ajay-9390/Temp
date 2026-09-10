package com.accreditation.nba.evidence.config;

import com.accreditation.nba.evidence.dto.response.StatisticsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * Cache names used by the module. Statistics are the only cacheable data and the app
 * functions correctly without Redis (default {@code spring.cache.type=none}).
 */
public final class CacheConfig {

    private CacheConfig() {
    }

    public static final String STATISTICS_CACHE = "evidence-statistics";
    public static final String GAP_CACHE = "evidence-gaps";

    /**
     * Redis-backed cache manager, active only when {@code spring.cache.type=redis}.
     */
    @Configuration
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
    static class RedisCacheConfig {

        @Bean
        public RedisCacheManager evidenceCacheManager(RedisConnectionFactory connectionFactory) {
            RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(10))
                    .disableCachingNullValues();

            // A type-bound serializer deserializes cached values back into StatisticsResponse
            // via its constructor, preserving the Long-typed maps across the round-trip
            // (a generic serializer would read them back as Integer and break serialization).
            RedisCacheConfiguration statisticsConfig = base.serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(
                            new Jackson2JsonRedisSerializer<>(new ObjectMapper(), StatisticsResponse.class)));

            return RedisCacheManager.builder(connectionFactory)
                    .cacheDefaults(base.serializeValuesWith(
                            RedisSerializationContext.SerializationPair.fromSerializer(
                                    new GenericJackson2JsonRedisSerializer())))
                    .withCacheConfiguration(STATISTICS_CACHE, statisticsConfig)
                    .build();
        }
    }
}
