package com.foodopia.backend.exception;

/**
 * Exception bei ungültigem Status-Übergang einer Transaktion
 */
public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(String message) {
        super(message);
    }

    public InvalidStatusTransitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
