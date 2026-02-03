package com.foodopia.backend.rest.v1.errorHandling;

import com.foodopia.backend.exception.*;
import com.foodopia.backend.rest.v1.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ErrorResponseHandler {

    private static final Logger logger = LoggerFactory.getLogger(ErrorResponseHandler.class);

    private ApiErrorResponse build(String message, String code) {
        String requestId = MDC.get("requestId");
        return ApiErrorResponse.builder()
                .errormessage(message)
                .errorCode(code)
                .errorUUID(requestId != null ? requestId : "unknown")
                .build();
    }

    @ExceptionHandler({AuthorizationResponseException.class, UserAlreadyExistingException.class, InvalidExceptionPassword.class, InvalidExceptionUsername.class})
    protected ResponseEntity<ApiErrorResponse> handleAuthorizationExceptions(AuthorizationResponseException e, HttpServletRequest request) {
        String code = String.valueOf(e.getCode());
        String msg = e.getMessage();
        logger.error("code={} message={}", code, msg);
        if (logger.isDebugEnabled()) logger.debug("stacktrace", e);
        return ResponseEntity.badRequest().body(build(msg, code));
    }

    @ExceptionHandler(AuthenticationException.class)
    protected ResponseEntity<ApiErrorResponse> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        String code = "AUTHENTICATION_FAILED";
        String msg = e.getMessage();
        logger.error("code={} message={}", code, msg);
        if (logger.isDebugEnabled()) logger.debug("stacktrace", e);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(build(msg, code));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String code = "INPUT_VALIDATION_ERROR";
        String errorMessage;
        String raw = e.getMessage() == null ? "" : e.getMessage();
        if (raw.contains("Blank") && raw.contains("email")) {
            errorMessage = "Please provide an email address";
        } else if (raw.contains("Pattern") && raw.contains("email")) {
            errorMessage = "Please provide a valid email address";
        } else if (raw.contains("Blank") && raw.contains("password")) {
            errorMessage = "Please provide a password";
        } else if (raw.contains("Pattern") && raw.contains("password")) {
            errorMessage = "Please provide a valid password";
        } else {
            errorMessage = "Some validation error occurred";
        }
        logger.error("code={} message={}", code, errorMessage);
        if (logger.isDebugEnabled()) logger.debug("stacktrace", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(build(errorMessage, code));
    }

    @ExceptionHandler(AddContactToListException.class)
    protected ResponseEntity<ApiErrorResponse> handleAddContactException(AddContactToListException e, HttpServletRequest request) {
        String code = "CONTACT_NOT_FOUND";
        String msg = e.getMessage();
        logger.error("code={} message={}", code, msg);
        if (logger.isDebugEnabled()) logger.debug("stacktrace", e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(msg, code));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiErrorResponse> handleGenericException(Exception e, HttpServletRequest request) {
        String code = "INTERNAL_ERROR";
        String msg = "Internal server error";
        logger.error("code={} message={}", code, e.getMessage());
        if (logger.isDebugEnabled()) logger.debug("stacktrace", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(msg, code));
    }
}
