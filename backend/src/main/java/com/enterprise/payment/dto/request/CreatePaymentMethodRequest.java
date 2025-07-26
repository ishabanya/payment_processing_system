package com.enterprise.payment.dto.request;

import com.enterprise.payment.entity.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentMethodRequest {

    @NotNull(message = "Payment method type is required")
    @JsonProperty("type")
    private PaymentMethod.PaymentMethodType type;

    @NotBlank(message = "Provider is required")
    @Size(max = 100, message = "Provider cannot exceed 100 characters")
    private String provider;

    @NotNull(message = "Payment details are required")
    private Map<String, Object> details;

    @Size(max = 4, message = "Last four digits must be exactly 4 characters")
    @Pattern(regexp = "^\\d{4}$", message = "Last four digits must be numeric")
    private String lastFourDigits;

    @Min(value = 1, message = "Expiry month must be between 1 and 12")
    @Max(value = 12, message = "Expiry month must be between 1 and 12")
    private Integer expiryMonth;

    @Min(value = 2024, message = "Expiry year must be current year or later")
    private Integer expiryYear;

    @JsonProperty("isDefault")
    private Boolean isDefault = false;

    @JsonProperty("isActive")
    private Boolean isActive = true;

    // Holder name for cards
    @Size(max = 100, message = "Cardholder name cannot exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Cardholder name can only contain letters, spaces, hyphens, and apostrophes")
    private String cardholderName;

    // Bank account specific fields
    @Size(max = 20, message = "Account number cannot exceed 20 characters")
    private String accountNumber;

    @Size(max = 20, message = "Routing number cannot exceed 20 characters")
    private String routingNumber;

    @Size(max = 100, message = "Bank name cannot exceed 100 characters")
    private String bankName;

    // Digital wallet specific fields
    @Email(message = "Wallet email must be valid")
    private String walletEmail;

    @Size(max = 50, message = "Wallet ID cannot exceed 50 characters")
    private String walletId;

    // Account ID for associating payment method with account
    private Long accountId;

    // Card specific fields
    @Size(max = 19, message = "Card number cannot exceed 19 characters")
    private String cardNumber;

    @Size(max = 4, message = "CVV cannot exceed 4 characters")
    private String cvv;

    // Business validation methods
    public boolean isValidForType() {
        return switch (type) {
            case CREDIT_CARD, DEBIT_CARD -> isValidCardDetails();
            case BANK_TRANSFER -> isValidBankDetails();
            case DIGITAL_WALLET -> isValidWalletDetails();
            case CRYPTO -> isValidCryptoDetails();
        };
    }

    private boolean isValidCardDetails() {
        return cardholderName != null && !cardholderName.trim().isEmpty() &&
               lastFourDigits != null && lastFourDigits.matches("\\d{4}") &&
               expiryMonth != null && expiryMonth >= 1 && expiryMonth <= 12 &&
               expiryYear != null && expiryYear >= java.time.Year.now().getValue();
    }

    private boolean isValidBankDetails() {
        return accountNumber != null && !accountNumber.trim().isEmpty() &&
               routingNumber != null && !routingNumber.trim().isEmpty() &&
               bankName != null && !bankName.trim().isEmpty();
    }

    private boolean isValidWalletDetails() {
        return (walletEmail != null && !walletEmail.trim().isEmpty()) ||
               (walletId != null && !walletId.trim().isEmpty());
    }

    private boolean isValidCryptoDetails() {
        return details != null && !details.isEmpty() &&
               details.containsKey("walletAddress");
    }

    public boolean isExpired() {
        if (expiryMonth == null || expiryYear == null) {
            return false;
        }
        java.time.LocalDate now = java.time.LocalDate.now();
        return now.getYear() > expiryYear || 
               (now.getYear() == expiryYear && now.getMonthValue() > expiryMonth);
    }
}