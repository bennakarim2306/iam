package com.foodopia.backend.exception;

/**
 * Exception wenn ein Item nicht gefunden wird
 */
public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(String message) {
        super(message);
    }

    public ItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
