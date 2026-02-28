package com.authflow.service.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory implementation of rate limit storage
 * Uses ConcurrentHashMap for thread-safe operations
 * Suitable for single-instance deployments
 */
@Slf4j
public class InMemoryRateLimitStorage implements RateLimitStorage {

	private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();

	@Override
	public int recordAttempt(String key, int timeWindowSeconds) {
		Instant now = Instant.now();

		RateLimitBucket bucket = buckets.compute(key, (k, existingBucket) -> {
			if (existingBucket == null) {
				// First request for this key
				RateLimitBucket newBucket = new RateLimitBucket(now, timeWindowSeconds);
				newBucket.increment();
				return newBucket;
			}

			if (existingBucket.isExpired(now)) {
				// Window expired, reset counter
				RateLimitBucket newBucket = new RateLimitBucket(now, timeWindowSeconds);
				newBucket.increment();
				return newBucket;
			}

			// Window still active
			existingBucket.increment();
			return existingBucket;
		});

		return bucket.getAttempts();
	}

	@Override
	public int getAttemptCount(String key, int timeWindowSeconds) {
		RateLimitBucket bucket = buckets.get(key);
		if (bucket == null) {
			return 0;
		}

		Instant now = Instant.now();
		if (bucket.isExpired(now)) {
			return 0;
		}

		return bucket.getAttempts();
	}

	@Override
	@Scheduled(fixedRate = 3600000) // 1 hour
	public void cleanupExpired() {
		Instant now = Instant.now();
		int removed = 0;

		var iterator = buckets.entrySet().iterator();
		while (iterator.hasNext()) {
			var entry = iterator.next();
			if (entry.getValue().isExpired(now)) {
				iterator.remove();
				removed++;
			}
		}

		if (removed > 0) {
			log.info("Cleaned up {} expired in-memory rate limit buckets", removed);
		}
	}

	/**
	 * Get the remaining seconds for a rate limit bucket
	 *
	 * @param key The rate limit key
	 * @return Remaining seconds, or 0 if bucket doesn't exist or expired
	 */
	public long getRemainingSeconds(String key) {
		RateLimitBucket bucket = buckets.get(key);
		if (bucket == null) {
			return 0;
		}

		Instant now = Instant.now();
		if (bucket.isExpired(now)) {
			return 0;
		}

		return bucket.getRemainingSeconds(now);
	}

	/**
	 * Internal class to hold rate limit bucket data
	 */
	private static class RateLimitBucket {
		private final Instant windowStart;
		private final int windowSeconds;
		private final AtomicInteger attempts;

		public RateLimitBucket(Instant windowStart, int windowSeconds) {
			this.windowStart = windowStart;
			this.windowSeconds = windowSeconds;
			this.attempts = new AtomicInteger(0);
		}

		public int increment() {
			return attempts.incrementAndGet();
		}

		public int getAttempts() {
			return attempts.get();
		}

		public boolean isExpired(Instant now) {
			return now.isAfter(windowStart.plusSeconds(windowSeconds));
		}

		public long getRemainingSeconds(Instant now) {
			Instant windowEnd = windowStart.plusSeconds(windowSeconds);
			long remaining = windowEnd.getEpochSecond() - now.getEpochSecond();
			return Math.max(0, remaining);
		}
	}
}
