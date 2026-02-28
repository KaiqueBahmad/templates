package com.authflow.service.storage;

/**
 * Interface for rate limit storage implementations
 * Allows switching between in-memory and Redis storage
 */
public interface RateLimitStorage {

	/**
	 * Record a rate limit attempt for the given key
	 *
	 * @param key The rate limit key (e.g., IP address or email)
	 * @return The number of attempts in the current time window after adding this attempt
	 */
	int recordAttempt(String key, int timeWindowSeconds);

	/**
	 * Get the current number of attempts for the given key within the time window
	 *
	 * @param key The rate limit key
	 * @param timeWindowSeconds The time window in seconds
	 * @return The number of attempts in the current time window
	 */
	int getAttemptCount(String key, int timeWindowSeconds);

	/**
	 * Clean up expired rate limit data
	 * Implementation-specific behavior
	 */
	void cleanupExpired();
}
