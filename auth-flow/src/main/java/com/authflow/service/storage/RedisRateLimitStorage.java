package com.authflow.service.storage;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Redis-based implementation of rate limit storage
 * Uses Redis sorted sets for sliding window rate limiting
 * Suitable for multi-instance deployments with shared rate limiting
 */
@Slf4j
@RequiredArgsConstructor
public class RedisRateLimitStorage implements RateLimitStorage {

	private final RedisTemplate<String, String> redisTemplate;
	private static final String KEY_PREFIX = "rate_limit:";

	/**
	 * Validate Redis connection on startup
	 * Fails fast if Redis is not available
	 */
	@PostConstruct
	public void validateConnection() {
		try {
			log.info("Testing Redis connection for rate limiting...");
			redisTemplate.getConnectionFactory().getConnection().ping();
			log.info("Redis connection successful - rate limiting ready");
		} catch (RedisConnectionFailureException e) {
			String errorMsg = String.format(
					"Failed to connect to Redis for rate limiting. " +
							"Please ensure Redis is running and accessible, or change " +
							"app.rate-limit.storage-type to MEMORY. Error: %s",
					e.getMessage()
			);
			log.error(errorMsg);
			throw new IllegalStateException(errorMsg, e);
		} catch (Exception e) {
			String errorMsg = String.format(
					"Failed to validate Redis connection for rate limiting: %s",
					e.getMessage()
			);
			log.error(errorMsg);
			throw new IllegalStateException(errorMsg, e);
		}
	}

	@Override
	public int recordAttempt(String key, int timeWindowSeconds) {
		String redisKey = KEY_PREFIX + key;
		long now = Instant.now().toEpochMilli();
		long windowStart = now - (timeWindowSeconds * 1000L);

		// Remove old entries outside the time window
		redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, windowStart);

		// Add current timestamp
		redisTemplate.opsForZSet().add(redisKey, String.valueOf(now), now);

		// Set expiration on the key (window + buffer)
		redisTemplate.expire(redisKey, timeWindowSeconds + 60, TimeUnit.SECONDS);

		// Count attempts in the current window
		Long count = redisTemplate.opsForZSet().zCard(redisKey);

		log.debug("Redis rate limit - Key: {}, Attempts: {}, Window: {}s", key, count, timeWindowSeconds);

		return count != null ? count.intValue() : 1;
	}

	@Override
	public int getAttemptCount(String key, int timeWindowSeconds) {
		String redisKey = KEY_PREFIX + key;
		long now = Instant.now().toEpochMilli();
		long windowStart = now - (timeWindowSeconds * 1000L);

		// Remove old entries outside the time window
		redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, windowStart);

		// Count attempts in the current window
		Long count = redisTemplate.opsForZSet().zCard(redisKey);

		return count != null ? count.intValue() : 0;
	}

	@Override
	public void cleanupExpired() {
		// Redis handles expiration automatically via TTL
		// This method is not needed for Redis implementation but kept for interface compatibility
		log.debug("Redis rate limit cleanup called (handled automatically by Redis TTL)");
	}

	/**
	 * Get the remaining seconds for a rate limit bucket
	 *
	 * @param key The rate limit key
	 * @param timeWindowSeconds The time window in seconds
	 * @return Remaining seconds, or 0 if no rate limit is active
	 */
	public long getRemainingSeconds(String key, int timeWindowSeconds) {
		String redisKey = KEY_PREFIX + key;
		long now = Instant.now().toEpochMilli();

		// Get the oldest entry in the current window
		var oldest = redisTemplate.opsForZSet().range(redisKey, 0, 0);

		if (oldest == null || oldest.isEmpty()) {
			return 0;
		}

		try {
			long oldestTimestamp = Long.parseLong(oldest.iterator().next());
			long windowEnd = oldestTimestamp + (timeWindowSeconds * 1000L);
			long remaining = (windowEnd - now) / 1000;
			return Math.max(0, remaining);
		} catch (NumberFormatException e) {
			log.warn("Failed to parse timestamp from Redis: {}", e.getMessage());
			return 0;
		}
	}
}
