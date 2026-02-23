package com.foodopia.backend.rest.v1.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

/**
 * Filter to wrap requests with ContentCachingRequestWrapper
 * This allows the request body to be logged by the interceptor
 * without consuming the stream for the actual handler.
 * 
 * NOTE: NOT registered as @Component. Enable only if needed and configured.
 * This filter MUST NOT wrap multipart requests as it breaks
 * Spring's multipart file upload parser and causes content-type corruption.
 */
public class RequestBodyLoggingFilter extends OncePerRequestFilter {

    @Value("${app.logging.request.log-body:false}")
    private boolean logBody;

    @Override
    protected void doFilterInternal(@org.springframework.lang.NonNull HttpServletRequest request, @org.springframework.lang.NonNull HttpServletResponse response, @org.springframework.lang.NonNull FilterChain filterChain) throws ServletException, IOException {
        // Only cache body if body logging is enabled AND it's safe to do so
        if (logBody && isSafeToCacheBody(request)) {
            request = new ContentCachingRequestWrapper(request);
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Determine if it's safe to cache this request body.
     * Returns false for ANY multipart content to avoid breaking Spring's multipart parser.
     */
    private boolean isSafeToCacheBody(HttpServletRequest request) {
        String contentType = request.getContentType();
        
        // NEVER cache any multipart content - critical for file uploads!
        if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
            return false;
        }
        
        // Only cache JSON/XML POST/PUT/PATCH requests
        String method = request.getMethod();
        return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method);
    }
}
