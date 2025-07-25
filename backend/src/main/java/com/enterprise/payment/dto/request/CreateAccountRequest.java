package com.enterprise.payment.dto.request;

import com.enterprise.payment.entity.Account;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    @NotBlank(message = "Account name is required")
    @Size(max = 255, message = "Account name cannot exceed 255 characters")
    private String accountName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid (E.164 format)")
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phone;

    @JsonProperty("status")
    private Account.AccountStatus status = Account.AccountStatus.ACTIVE;

    @DecimalMin(value = "0.0", inclusive = true, message = "Initial balance must be non-negative")
    @Digits(integer = 17, fraction = 2, message = "Balance must have at most 2 decimal places")
    private BigDecimal initialBalance = BigDecimal.ZERO;

    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be 3 uppercase letters")
    private String currencyCode = "USD";

    @Size(max = 255, message = "Created by field cannot exceed 255 characters")
    private String createdBy;

    // Business validation methods
    public boolean isValidForCreation() {
        return accountName != null && !accountName.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               currencyCode != null && currencyCode.length() == 3;
    }

    public boolean hasValidContactInfo() {
        return (email != null && !email.trim().isEmpty()) ||
               (phone != null && !phone.trim().isEmpty());
    }
}