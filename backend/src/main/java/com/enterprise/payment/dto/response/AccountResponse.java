package com.enterprise.payment.dto.response;

import com.enterprise.payment.entity.Account;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("accountName")
    private String accountName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("status")
    private Account.AccountStatus status;

    @JsonProperty("balance")
    private BigDecimal balance;

    @JsonProperty("currencyCode")
    private String currencyCode;

    @JsonProperty("createdAt")
    private OffsetDateTime createdAt;

    @JsonProperty("updatedAt")
    private OffsetDateTime updatedAt;

    @JsonProperty("createdBy")
    private String createdBy;

    @JsonProperty("updatedBy")
    private String updatedBy;

    // Related entities (included when requested)
    @JsonProperty("users")
    private List<UserResponse> users;

    @JsonProperty("paymentMethods")
    private List<PaymentMethodResponse> paymentMethods;

    // Statistics (computed fields)
    @JsonProperty("totalPayments")
    private Long totalPayments;

    @JsonProperty("totalPaymentAmount")
    private BigDecimal totalPaymentAmount;

    @JsonProperty("successfulPayments")
    private Long successfulPayments;

    @JsonProperty("successfulPaymentAmount")
    private BigDecimal successfulPaymentAmount;

    @JsonProperty("failedPayments")
    private Long failedPayments;

    @JsonProperty("pendingPayments")
    private Long pendingPayments;

    @JsonProperty("pendingPaymentAmount")
    private BigDecimal pendingPaymentAmount;

    @JsonProperty("averagePaymentAmount")
    private BigDecimal averagePaymentAmount;

    @JsonProperty("lastPaymentDate")
    private OffsetDateTime lastPaymentDate;

    @JsonProperty("activePaymentMethods")
    private Integer activePaymentMethods;

    // Factory method to create from Account entity
    public static AccountResponse fromEntity(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountName(account.getAccountName());
        response.setEmail(account.getEmail());
        response.setPhone(account.getPhone());
        response.setStatus(account.getStatus());
        response.setBalance(account.getBalance());
        response.setCurrencyCode(account.getCurrencyCode());
        response.setCreatedAt(account.getCreatedAt());
        response.setUpdatedAt(account.getUpdatedAt());
        response.setCreatedBy(account.getCreatedBy());
        response.setUpdatedBy(account.getUpdatedBy());
        
        return response;
    }

    // Factory method with related entities
    public static AccountResponse fromEntityWithDetails(Account account, boolean includeUsers, 
                                                       boolean includePaymentMethods) {
        AccountResponse response = fromEntity(account);
        
        if (includeUsers && account.getUsers() != null) {
            response.setUsers(
                account.getUsers().stream()
                    .map(UserResponse::fromEntity)
                    .toList()
            );
        }
        
        if (includePaymentMethods && account.getPaymentMethods() != null) {
            response.setPaymentMethods(
                account.getPaymentMethods().stream()
                    .filter(pm -> pm.getIsActive())
                    .map(PaymentMethodResponse::fromEntity)
                    .toList()
            );
            
            response.setActivePaymentMethods(
                (int) account.getPaymentMethods().stream()
                    .filter(pm -> pm.getIsActive())
                    .count()
            );
        }
        
        // Calculate payment statistics
        if (account.getPayments() != null) {
            response.setTotalPayments((long) account.getPayments().size());
            
            BigDecimal totalAmount = account.getPayments().stream()
                .map(p -> p.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            response.setTotalPaymentAmount(totalAmount);
            
            long successfulCount = account.getPayments().stream()
                .filter(p -> p.getStatus() == com.enterprise.payment.entity.Payment.PaymentStatus.COMPLETED)
                .count();
            response.setSuccessfulPayments(successfulCount);
            
            BigDecimal successfulAmount = account.getPayments().stream()
                .filter(p -> p.getStatus() == com.enterprise.payment.entity.Payment.PaymentStatus.COMPLETED)
                .map(p -> p.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            response.setSuccessfulPaymentAmount(successfulAmount);
            
            long failedCount = account.getPayments().stream()
                .filter(p -> p.getStatus() == com.enterprise.payment.entity.Payment.PaymentStatus.FAILED)
                .count();
            response.setFailedPayments(failedCount);
            
            long pendingCount = account.getPayments().stream()
                .filter(p -> p.getStatus() == com.enterprise.payment.entity.Payment.PaymentStatus.PENDING ||
                           p.getStatus() == com.enterprise.payment.entity.Payment.PaymentStatus.PROCESSING)
                .count();
            response.setPendingPayments(pendingCount);
            
            BigDecimal pendingAmount = account.getPayments().stream()
                .filter(p -> p.getStatus() == com.enterprise.payment.entity.Payment.PaymentStatus.PENDING ||
                           p.getStatus() == com.enterprise.payment.entity.Payment.PaymentStatus.PROCESSING)
                .map(p -> p.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            response.setPendingPaymentAmount(pendingAmount);
            
            // Calculate average payment amount
            if (response.getTotalPayments() > 0) {
                response.setAveragePaymentAmount(
                    totalAmount.divide(BigDecimal.valueOf(response.getTotalPayments()), 2, java.math.RoundingMode.HALF_UP)
                );
            }
            
            // Get last payment date
            response.setLastPaymentDate(
                account.getPayments().stream()
                    .map(p -> p.getCreatedAt())
                    .max(OffsetDateTime::compareTo)
                    .orElse(null)
            );
        }
        
        return response;
    }

    // Factory method for minimal response (without sensitive data)
    public static AccountResponse fromEntityMinimal(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountName(account.getAccountName());
        response.setStatus(account.getStatus());
        response.setCurrencyCode(account.getCurrencyCode());
        response.setCreatedAt(account.getCreatedAt());
        
        return response;
    }
}