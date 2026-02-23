package com.foodopia.backend.rest.v1.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    @Value("${app.logging.request.enabled:true}")
    private boolean loggingEnabled;

    @Value("${app.logging.request.log-headers:false}")
    private boolean logHeaders;

    @Value("${app.logging.request.log-body:false}")
    private boolean logBody;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        if (!loggingEnabled) {
            return true;
        }

        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);

        StringBuilder logMessage = new StringBuilder();
        logMessage.append("\n---- INCOMING REQUEST ----\n");
        logMessage.append(String.format("- METHOD: %s\n", request.getMethod()));
        logMessage.append(String.format("- URI: %s\n", request.getRequestURI()));

        if (request.getQueryString() != null) {
            logMessage.append(String.format("- QUERY: %s\n", request.getQueryString()));
        }

        logMessage.append(String.format("- REMOTE_ADDR: %s\n", getClientIp(request)));
        logMessage.append(String.format("- CONTENT_TYPE: %s\n", request.getContentType()));

        if (logHeaders) {
            logMessage.append("- HEADERS:\n");
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                logMessage.append(String.format("-   %s: %s\n", headerName, headerValue));
            }
        }

        if (logBody && shouldLogBody(request)) {
            String body = getRequestBody(request);
            if (!body.isEmpty()) {
                logMessage.append(String.format("- BODY: %s\n", truncateIfNeeded(body, 1000)));
            }
        }

        logMessage.append("---- END OF REQUEST ----\n");

        logger.info(logMessage.toString());

        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        if (!loggingEnabled) {
            return;
        }

        long startTime = (long) request.getAttribute("startTime");
        long duration = System.currentTimeMillis() - startTime;

        StringBuilder logMessage = new StringBuilder();
        logMessage.append("\n---- REQUEST COMPLETED ----\n");
        logMessage.append(String.format("- METHOD: %s\n", request.getMethod()));
        logMessage.append(String.format("- URI: %s\n", request.getRequestURI()));
        logMessage.append(String.format("- STATUS: %d\n", response.getStatus()));
        logMessage.append(String.format("- DURATION: %dms\n", duration));

        if (ex != null) {
            logMessage.append(String.format("- EXCEPTION: %s\n", ex.getMessage()));
        }

        logMessage.append("---- END OF REQUEST ----\n");

        logger.info(logMessage.toString());
    }

    /**
     * Determine if request body should be logged.
     * Note: multipart/form-data is excluded from body caching
     * by RequestBodyLoggingFilter to avoid interfering with Spring's parser
     */
    private boolean shouldLogBody(HttpServletRequest request) {
        String method = request.getMethod();
        if (!("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method))) {
            return false;
        }
        
        String contentType = request.getContentType();
        if (contentType == null) {
            return false;
        }
        
        // Skip multipart/form-data as it's not cached by the filter
        return true; // !contentType.startsWith("multipart/form-data");
    }

    /**
     * Get cached request body if available
     */
    private String getRequestBody(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper) {
            byte[] buf = ((ContentCachingRequestWrapper) request).getContentAsByteArray();
            if (buf.length > 0) {
                return new String(buf, StandardCharsets.UTF_8);
            }
        }
        return "";
    }

    /**
     * Truncate body if too long for logging
     */
    private String truncateIfNeeded(String str, int maxLength) {
        if (str.length() > maxLength) {
            return str.substring(0, maxLength) + "... (truncated)";
        }
        return str;
    }

    /**
     * Get client IP address, considering proxy headers
     */
    private String getClientIp(HttpServletRequest request) {
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
}