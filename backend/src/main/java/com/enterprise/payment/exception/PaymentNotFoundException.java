package com.enterprise.payment.exception;

/**
 * Exception thrown when a payment is not found
 */
public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(String message) {
        super(message);
    }

    public static PaymentNotFoundException byId(String paymentId) {
        return new PaymentNotFoundException("Payment not found with ID: " + paymentId);
    }

    public PaymentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}