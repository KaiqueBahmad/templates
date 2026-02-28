package com.authflow.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.authflow.annotation.RateLimit;
import com.authflow.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

	private final RateLimitService rateLimitService;
	private final ObjectMapper objectMapper;

	@Value("${app.rate-limit.enabled:true}")
	private boolean rateLimitEnabled;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		// Skip rate limiting if globally disabled
		if (!rateLimitEnabled) {
			log.debug("Rate limiting is disabled globally");
			return true;
		}

		if (!(handler instanceof HandlerMethod)) {
			return true;
		}

		HandlerMethod handlerMethod = (HandlerMethod) handler;

		// Get all @RateLimit annotations (supports repeatable annotations)
		RateLimit[] rateLimits = handlerMethod.getMethod().getDeclaredAnnotationsByType(RateLimit.class);

		if (rateLimits.length == 0) {
			return true;
		}

		// Check all rate limits - if any fails, exception is thrown
		for (RateLimit rateLimit : rateLimits) {
			String key = extractKey(request, rateLimit);
			if (key == null) {
				log.warn("Could not extract rate limit key ({}) for endpoint: {}",
						rateLimit.keyType(), request.getRequestURI());
				continue;
			}

			// This will throw RateLimitExceededException if limit is exceeded
			rateLimitService.checkRateLimit(key, rateLimit.maxAttempts(), rateLimit.timeWindowSeconds());
		}

		return true;
	}

	private String extractKey(HttpServletRequest request, RateLimit rateLimit) {
		if (rateLimit.keyType() == RateLimit.KeyType.IP) {
			return getClientIpAddress(request);
		} else if (rateLimit.keyType() == RateLimit.KeyType.EMAIL) {
			return extractEmailFromRequest(request);
		} else if (rateLimit.keyType() == RateLimit.KeyType.IP_PER_ROUTE) {
			String ip = getClientIpAddress(request);
			String route = request.getMethod() + ":" + request.getRequestURI();
			return ip + ":" + route;
		}
		return null;
	}

	private String getClientIpAddress(HttpServletRequest request) {
		String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
			return xForwardedFor.split(",")[0].trim();
		}

		String xRealIp = request.getHeader("X-Real-IP");
		if (xRealIp != null && !xRealIp.isEmpty()) {
			return xRealIp;
		}

		return request.getRemoteAddr();
	}

	private String extractEmailFromRequest(HttpServletRequest request) {
		// All requests are wrapped by CachingRequestBodyFilter
		if (!(request instanceof ContentCachingRequestWrapper)) {
			log.warn("Request is not wrapped in ContentCachingRequestWrapper");
			return null;
		}

		try {
			ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
			byte[] content = wrapper.getContentAsByteArray();

			if (content.length == 0) {
				return null;
			}

			Map<String, Object> body = objectMapper.readValue(content, Map.class);
			Object email = body.get("email");
			return email != null ? email.toString() : null;
		} catch (IOException e) {
			log.warn("Failed to extract email from request body", e);
			return null;
		}
	}
}
