package com.foodopia.backend.exception;

/**
 * Exception wenn die verfügbare Menge eines Items nicht ausreichend ist
 */
public class InsufficientQuantityException extends RuntimeException {
    public InsufficientQuantityException(String message) {
        super(message);
    }

    public InsufficientQuantityException(String message, Throwable cause) {
        super(message, cause);
    }
}
