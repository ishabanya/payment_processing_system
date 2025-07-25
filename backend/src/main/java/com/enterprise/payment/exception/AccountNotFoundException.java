package com.enterprise.payment.exception;

/**
 * Exception thrown when an account is not found
 */
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String message) {
        super(message);
    }

    public static AccountNotFoundException byId(Long accountId) {
        return new AccountNotFoundException("Account not found with ID: " + accountId);
    }

    public static AccountNotFoundException byAccountNumber(String accountNumber) {
        return new AccountNotFoundException("Account not found with account number: " + accountNumber);
    }

    public AccountNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}