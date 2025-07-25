package com.enterprise.payment.exception;

/**
 * Base exception class for all payment system exceptions
 */
public abstract class PaymentSystemException extends RuntimeException {
    
    private final String errorCode;
    
    protected PaymentSystemException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    protected PaymentSystemException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}