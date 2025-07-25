package com.enterprise.payment.dto.response;

import com.enterprise.payment.entity.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentMethodResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("type")
    private PaymentMethod.PaymentMethodType type;

    @JsonProperty("provider")
    private String provider;

    @JsonProperty("lastFourDigits")
    private String lastFourDigits;

    @JsonProperty("maskedDetails")
    private String maskedDetails;

    @JsonProperty("expiryMonth")
    private Integer expiryMonth;

    @JsonProperty("expiryYear")
    private Integer expiryYear;

    @JsonProperty("isDefault")
    private Boolean isDefault;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("isExpired")
    private Boolean isExpired;

    @JsonProperty("createdAt")
    private OffsetDateTime createdAt;

    @JsonProperty("updatedAt")
    private OffsetDateTime updatedAt;

    // Account information (minimal)
    @JsonProperty("account")
    private AccountSummary account;

    // Additional display information
    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("cardBrand")
    private String cardBrand;

    @JsonProperty("bankName")
    private String bankName;

    @JsonProperty("walletProvider")
    private String walletProvider;

    // Usage statistics
    @JsonProperty("totalPayments")
    private Long totalPayments;

    @JsonProperty("successfulPayments")
    private Long successfulPayments;

    @JsonProperty("lastUsedAt")
    private OffsetDateTime lastUsedAt;

    // Factory method to create from PaymentMethod entity
    public static PaymentMethodResponse fromEntity(PaymentMethod paymentMethod) {
        PaymentMethodResponse response = new PaymentMethodResponse();
        response.setId(paymentMethod.getId());
        response.setType(paymentMethod.getType());
        response.setProvider(paymentMethod.getProvider());
        response.setLastFourDigits(paymentMethod.getLastFourDigits());
        response.setMaskedDetails(paymentMethod.getMaskedDetails());
        response.setExpiryMonth(paymentMethod.getExpiryMonth());
        response.setExpiryYear(paymentMethod.getExpiryYear());
        response.setIsDefault(paymentMethod.getIsDefault());
        response.setIsActive(paymentMethod.getIsActive());
        response.setIsExpired(paymentMethod.isExpired());
        response.setCreatedAt(paymentMethod.getCreatedAt());
        response.setUpdatedAt(paymentMethod.getUpdatedAt());
        
        // Generate display name based on type
        response.setDisplayName(generateDisplayName(paymentMethod));
        
        return response;
    }

    // Factory method with account information
    public static PaymentMethodResponse fromEntityWithAccount(PaymentMethod paymentMethod) {
        PaymentMethodResponse response = fromEntity(paymentMethod);
        
        if (paymentMethod.getAccount() != null) {
            AccountSummary accountSummary = new AccountSummary();
            accountSummary.setId(paymentMethod.getAccount().getId());
            accountSummary.setAccountNumber(paymentMethod.getAccount().getAccountNumber());
            accountSummary.setAccountName(paymentMethod.getAccount().getAccountName());
            accountSummary.setStatus(paymentMethod.getAccount().getStatus());
            response.setAccount(accountSummary);
        }
        
        return response;
    }

    // Factory method with usage statistics
    public static PaymentMethodResponse fromEntityWithStats(PaymentMethod paymentMethod) {
        PaymentMethodResponse response = fromEntityWithAccount(paymentMethod);
        
        if (paymentMethod.getPayments() != null) {
            response.setTotalPayments((long) paymentMethod.getPayments().size());
            
            long successfulCount = paymentMethod.getPayments().stream()
                .filter(p -> p.getStatus() == com.enterprise.payment.entity.Payment.PaymentStatus.COMPLETED)
                .count();
            response.setSuccessfulPayments(successfulCount);
            
            response.setLastUsedAt(
                paymentMethod.getPayments().stream()
                    .map(p -> p.getCreatedAt())
                    .max(OffsetDateTime::compareTo)
                    .orElse(null)
            );
        }
        
        return response;
    }

    // Factory method for minimal response (without account information)
    public static PaymentMethodResponse fromEntityMinimal(PaymentMethod paymentMethod) {
        PaymentMethodResponse response = new PaymentMethodResponse();
        response.setId(paymentMethod.getId());
        response.setType(paymentMethod.getType());
        response.setProvider(paymentMethod.getProvider());
        response.setMaskedDetails(paymentMethod.getMaskedDetails());
        response.setIsDefault(paymentMethod.getIsDefault());
        response.setIsActive(paymentMethod.getIsActive());
        response.setIsExpired(paymentMethod.isExpired());
        response.setDisplayName(generateDisplayName(paymentMethod));
        
        if (paymentMethod.getType() == PaymentMethod.PaymentMethodType.CREDIT_CARD ||
            paymentMethod.getType() == PaymentMethod.PaymentMethodType.DEBIT_CARD) {
            response.setExpiryMonth(paymentMethod.getExpiryMonth());
            response.setExpiryYear(paymentMethod.getExpiryYear());
        }
        
        return response;
    }

    // Helper method to generate display name
    private static String generateDisplayName(PaymentMethod paymentMethod) {
        return switch (paymentMethod.getType()) {
            case CREDIT_CARD -> paymentMethod.getProvider() + " •••• " + paymentMethod.getLastFourDigits();
            case DEBIT_CARD -> paymentMethod.getProvider() + " Debit •••• " + paymentMethod.getLastFourDigits();
            case BANK_TRANSFER -> paymentMethod.getProvider() + " Bank Account";
            case DIGITAL_WALLET -> paymentMethod.getProvider() + " Wallet";
            case CRYPTO -> paymentMethod.getProvider() + " Crypto Wallet";
        };
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AccountSummary {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("accountNumber")
        private String accountNumber;

        @JsonProperty("accountName")
        private String accountName;

        @JsonProperty("status")
        private com.enterprise.payment.entity.Account.AccountStatus status;
    }
}