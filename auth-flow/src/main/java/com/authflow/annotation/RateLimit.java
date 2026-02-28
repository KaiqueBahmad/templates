package com.authflow.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RateLimits.class)
public @interface RateLimit {

	/**
	 * Maximum number of requests allowed within the time window
	 */
	int maxAttempts() default 5;

	/**
	 * Time window in seconds
	 */
	int timeWindowSeconds() default 3600;

	/**
	 * Rate limit key type (IP, EMAIL, or IP_PER_ROUTE)
	 */
	KeyType keyType() default KeyType.IP;

	enum KeyType {
		IP,             // Rate limit by IP address (shared across routes)
		EMAIL,          // Rate limit by email from request body (shared across routes)
		IP_PER_ROUTE    // Rate limit by IP address per route (isolated per endpoint)
	}
}
