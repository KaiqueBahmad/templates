package com.authflow.config;

import com.authflow.service.storage.InMemoryRateLimitStorage;
import com.authflow.service.storage.RateLimitStorage;
import com.authflow.service.storage.RedisRateLimitStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Configuration for rate limit storage
 * Explicitly configured via app.rate-limit.storage-type property
 */
@Configuration
@Slf4j
public class RateLimitStorageConfig {

	/**
	 * Create Redis-based rate limit storage when storage-type is REDIS
	 * Requires Redis to be configured in application.yml
	 * If Redis is not available, application will fail to start
	 */
	@Bean
	@ConditionalOnProperty(name = "app.rate-limit.storage-type", havingValue = "REDIS")
	public RateLimitStorage redisRateLimitStorage(RedisTemplate<String, String> redisTemplate) {
		log.info("Using Redis-based rate limiting (distributed) - storage-type: REDIS");
		return new RedisRateLimitStorage(redisTemplate);
	}

	/**
	 * Create in-memory rate limit storage when storage-type is MEMORY (default)
	 * Used for single instance deployments
	 */
	@Bean
	@ConditionalOnProperty(name = "app.rate-limit.storage-type", havingValue = "MEMORY", matchIfMissing = true)
	public RateLimitStorage inMemoryRateLimitStorage() {
		log.info("Using in-memory rate limiting (single instance) - storage-type: MEMORY");
		return new InMemoryRateLimitStorage();
	}
}
