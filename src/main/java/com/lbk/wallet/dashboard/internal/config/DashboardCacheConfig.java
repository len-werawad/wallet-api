package com.lbk.wallet.dashboard.internal.config;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Configuration for dashboard async processing and cache customization
 * Note: CacheManager is already configured in CacheConfig, this class only adds dashboard-specific configs
 */
@Configuration
@EnableAsync
public class DashboardCacheConfig {

    /**
     * Task executor for async event processing
     */
    @Bean(name = "dashboardEventExecutor")
    public Executor dashboardEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("dashboard-event-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * Dashboard-specific cache configuration
     * This enhances the existing RedisCacheManager with dashboard-specific cache settings
     */
    @Bean(name = "dashboardCacheManager")
    public CacheManager dashboardCacheManager(RedisConnectionFactory connectionFactory) {

        // Default configuration for dashboard caches
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15))  // 15 minutes TTL
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer())
                )
                .disableCachingNullValues();

        // Specific configurations for different dashboard cache types
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Dashboard data - cache for 15 minutes
        cacheConfigurations.put("dashboardData",
                defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // Account balances - cache for 5 minutes (more frequently updated)
        cacheConfigurations.put("accountBalances",
                defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // Transaction summary - cache for 30 minutes
        cacheConfigurations.put("transactionSummary",
                defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Financial summary - cache for 1 hour
        cacheConfigurations.put("financialSummary",
                defaultConfig.entryTtl(Duration.ofHours(1)));

        // User statistics - cache for 2 hours
        cacheConfigurations.put("userStatistics",
                defaultConfig.entryTtl(Duration.ofHours(2)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
