package com.authflow.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

/**
 * Filter to wrap HttpServletRequest with ContentCachingRequestWrapper
 * This allows reading the request body multiple times (for rate limiting and controller)
 */
@Component
public class CachingRequestBodyFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		// Wrap request to cache the body
		ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);

		filterChain.doFilter(wrappedRequest, response);
	}
}
