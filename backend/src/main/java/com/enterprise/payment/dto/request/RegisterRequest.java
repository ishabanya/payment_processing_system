package com.enterprise.payment.dto.request;

import com.enterprise.payment.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    // User information
    @Valid
    @NotNull(message = "User information is required")
    private UserInfo userInfo;

    // Account information (optional for admin users)
    @Valid
    private AccountInfo accountInfo;

    // Terms and conditions acceptance
    @AssertTrue(message = "You must accept the terms and conditions")
    private Boolean acceptTerms;

    // Privacy policy acceptance
    @AssertTrue(message = "You must accept the privacy policy")
    private Boolean acceptPrivacy;

    // Marketing emails opt-in (optional)
    private Boolean marketingOptIn = false;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "Username can only contain letters, numbers, dots, hyphens, and underscores")
        private String username;

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email cannot exceed 255 characters")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
        @Pattern.List({
            @Pattern(regexp = ".*[a-z].*", message = "Password must contain at least one lowercase letter"),
            @Pattern(regexp = ".*[A-Z].*", message = "Password must contain at least one uppercase letter"),
            @Pattern(regexp = ".*\\d.*", message = "Password must contain at least one digit"),
            @Pattern(regexp = ".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*", message = "Password must contain at least one special character")
        })
        private String password;

        @NotBlank(message = "Password confirmation is required")
        private String confirmPassword;

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name cannot exceed 100 characters")
        @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name can only contain letters, spaces, hyphens, and apostrophes")
        private String firstName;

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name cannot exceed 100 characters")
        @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
        private String lastName;

        @NotNull(message = "Role is required")
        @JsonProperty("role")
        private User.UserRole role = User.UserRole.USER;

        // Custom validation method
        public boolean isPasswordMatching() {
            return password != null && password.equals(confirmPassword);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountInfo {
        @NotBlank(message = "Account name is required")
        @Size(max = 255, message = "Account name cannot exceed 255 characters")
        private String accountName;

        @NotBlank(message = "Account email is required")
        @Email(message = "Account email must be valid")
        @Size(max = 255, message = "Account email cannot exceed 255 characters")
        private String email;

        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid (E.164 format)")
        @Size(max = 20, message = "Phone number cannot exceed 20 characters")
        private String phone;

        @NotBlank(message = "Currency code is required")
        @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be 3 uppercase letters")
        private String currencyCode = "USD";

        // Business/Organization details
        @Size(max = 255, message = "Company name cannot exceed 255 characters")
        private String companyName;

        @Size(max = 100, message = "Industry cannot exceed 100 characters")
        private String industry;

        @Size(max = 500, message = "Business description cannot exceed 500 characters")
        private String businessDescription;
    }

    // Business validation methods
    public boolean isValidRegistration() {
        return userInfo != null && userInfo.isPasswordMatching() &&
               acceptTerms != null && acceptTerms &&
               acceptPrivacy != null && acceptPrivacy &&
               (userInfo.role == User.UserRole.ADMIN || accountInfo != null);
    }

    public boolean requiresAccountCreation() {
        return userInfo != null && userInfo.role != User.UserRole.ADMIN;
    }

    public boolean isBusinessRegistration() {
        return accountInfo != null && 
               (accountInfo.companyName != null && !accountInfo.companyName.trim().isEmpty());
    }

    // Convert to CreateUserRequest
    public CreateUserRequest toCreateUserRequest() {
        if (userInfo == null) return null;
        
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername(userInfo.username);
        request.setEmail(userInfo.email);
        request.setPassword(userInfo.password);
        request.setFirstName(userInfo.firstName);
        request.setLastName(userInfo.lastName);
        request.setRole(userInfo.role);
        request.setIsActive(true);
        
        return request;
    }

    // Convert to CreateAccountRequest
    public CreateAccountRequest toCreateAccountRequest() {
        if (accountInfo == null) return null;
        
        CreateAccountRequest request = new CreateAccountRequest();
        request.setAccountName(accountInfo.accountName);
        request.setEmail(accountInfo.email);
        request.setPhone(accountInfo.phone);
        request.setCurrencyCode(accountInfo.currencyCode);
        request.setCreatedBy(userInfo != null ? userInfo.username : null);
        
        return request;
    }
}