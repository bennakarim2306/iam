package com.foodopia.backend.exception;

/**
 * Exception wenn eine Transaktion nicht gefunden wird
 */
public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(String message) {
        super(message);
    }

    public TransactionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
