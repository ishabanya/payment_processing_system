package com.enterprise.payment.exception;

public class UserNotFoundException extends PaymentSystemException {
    
    public UserNotFoundException(String username) {
        super("User not found with username: " + username, "USER_NOT_FOUND");
    }
    
    public UserNotFoundException(Long userId) {
        super("User not found with ID: " + userId, "USER_NOT_FOUND");
    }
}