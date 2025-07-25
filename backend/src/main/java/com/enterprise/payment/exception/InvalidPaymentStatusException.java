package com.enterprise.payment.exception;

/**
 * Exception thrown when attempting to perform an operation with an invalid payment status
 */
public class InvalidPaymentStatusException extends RuntimeException {

    public InvalidPaymentStatusException(String message) {
        super(message);
    }

    public InvalidPaymentStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}