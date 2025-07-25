package com.enterprise.payment.exception;

import java.math.BigDecimal;

/**
 * Exception thrown when there are insufficient funds for a transaction
 */
public class InsufficientFundsException extends RuntimeException {

    private final BigDecimal availableBalance;
    private final BigDecimal requestedAmount;

    public InsufficientFundsException(String message) {
        super(message);
        this.availableBalance = null;
        this.requestedAmount = null;
    }

    public InsufficientFundsException(BigDecimal availableBalance, BigDecimal requestedAmount) {
        super(String.format("Insufficient funds. Available: %s, Requested: %s", 
                availableBalance, requestedAmount));
        this.availableBalance = availableBalance;
        this.requestedAmount = requestedAmount;
    }

    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
        this.availableBalance = null;
        this.requestedAmount = null;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }
}