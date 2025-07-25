package com.enterprise.payment.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username or email is required")
    @Size(max = 255, message = "Username or email cannot exceed 255 characters")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    @Size(max = 255, message = "Password cannot exceed 255 characters")
    private String password;

    // For "Remember Me" functionality
    private Boolean rememberMe = false;

    // Client information for security logging
    @Size(max = 255, message = "User agent cannot exceed 255 characters")
    private String userAgent;

    @Pattern(regexp = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$|^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$", 
             message = "IP address must be valid IPv4 or IPv6 format")
    private String ipAddress;

    // Business validation methods
    public boolean isEmailLogin() {
        return usernameOrEmail != null && usernameOrEmail.contains("@");
    }

    public boolean isUsernameLogin() {
        return usernameOrEmail != null && !usernameOrEmail.contains("@");
    }

    public boolean isValidCredentials() {
        return usernameOrEmail != null && !usernameOrEmail.trim().isEmpty() &&
               password != null && !password.trim().isEmpty();
    }

    // For security purposes, don't include password in toString
    @Override
    public String toString() {
        return "LoginRequest{" +
                "usernameOrEmail='" + usernameOrEmail + '\'' +
                ", password='[PROTECTED]'" +
                ", rememberMe=" + rememberMe +
                ", userAgent='" + userAgent + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}