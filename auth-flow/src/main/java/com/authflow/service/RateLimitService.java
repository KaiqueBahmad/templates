package com.authflow.service;

import com.authflow.exception.RateLimitExceededException;
import com.authflow.service.storage.InMemoryRateLimitStorage;
import com.authflow.service.storage.RateLimitStorage;
import com.authflow.service.storage.RedisRateLimitStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RateLimitService {

	private final RateLimitStorage storage;

	/**
	 * Check if the request is allowed based on rate limit
	 *
	 * @param key Unique identifier (IP address or email)
	 * @param maxAttempts Maximum number of attempts allowed
	 * @param timeWindowSeconds Time window in seconds
	 * @throws RateLimitExceededException if rate limit is exceeded
	 */
	public void checkRateLimit(String key, int maxAttempts, int timeWindowSeconds) {
		// Record the attempt and get current count
		int attempts = storage.recordAttempt(key, timeWindowSeconds);

		// Check if limit is exceeded
		if (attempts > maxAttempts) {
			long remainingSeconds = getRemainingSeconds(key, timeWindowSeconds);
			throw new RateLimitExceededException(
					String.format("Rate limit exceeded. Too many requests. Please try again in %d seconds.", remainingSeconds)
			);
		}

		log.debug("Rate limit check for key '{}': {} attempts allowed, current count: {}",
				key, maxAttempts, attempts);
	}

	/**
	 * Get remaining seconds for a rate limit key
	 * Supports both in-memory and Redis storage implementations
	 */
	private long getRemainingSeconds(String key, int timeWindowSeconds) {
		if (storage instanceof InMemoryRateLimitStorage) {
			return ((InMemoryRateLimitStorage) storage).getRemainingSeconds(key);
		} else if (storage instanceof RedisRateLimitStorage) {
			return ((RedisRateLimitStorage) storage).getRemainingSeconds(key, timeWindowSeconds);
		}
		// Default fallback
		return timeWindowSeconds;
	}
}
