package com.foodopia.backend.exception;

/**
 * Exception wenn der Benutzer nicht berechtigt ist, diese Operation durchzuführen
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
