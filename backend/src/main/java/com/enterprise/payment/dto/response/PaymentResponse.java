package com.enterprise.payment.dto.response;

import com.enterprise.payment.entity.Payment;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("paymentReference")
    private String paymentReference;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("currencyCode")
    private String currencyCode;

    @JsonProperty("description")
    private String description;

    @JsonProperty("status")
    private Payment.PaymentStatus status;

    @JsonProperty("merchantReference")
    private String merchantReference;

    @JsonProperty("callbackUrl")
    private String callbackUrl;

    @JsonProperty("successUrl")
    private String successUrl;

    @JsonProperty("failureUrl")
    private String failureUrl;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    @JsonProperty("riskScore")
    private BigDecimal riskScore;

    @JsonProperty("processedAt")
    private OffsetDateTime processedAt;

    @JsonProperty("expiresAt")
    private OffsetDateTime expiresAt;

    @JsonProperty("createdAt")
    private OffsetDateTime createdAt;

    @JsonProperty("updatedAt")
    private OffsetDateTime updatedAt;

    // Related entities (will be included when requested)
    @JsonProperty("account")
    private AccountResponse account;

    @JsonProperty("paymentMethod")
    private PaymentMethodResponse paymentMethod;

    @JsonProperty("transactions")
    private List<TransactionResponse> transactions;

    @JsonProperty("statusHistory")
    private List<PaymentStatusHistoryResponse> statusHistory;

    // Computed fields
    @JsonProperty("isExpired")
    private Boolean isExpired;

    @JsonProperty("canBeProcessed")
    private Boolean canBeProcessed;

    @JsonProperty("canBeRefunded")
    private Boolean canBeRefunded;

    @JsonProperty("canBeCancelled")
    private Boolean canBeCancelled;

    @JsonProperty("totalTransactionAmount")
    private BigDecimal totalTransactionAmount;

    @JsonProperty("successfulTransactionAmount")
    private BigDecimal successfulTransactionAmount;

    @JsonProperty("refundedAmount")
    private BigDecimal refundedAmount;

    @JsonProperty("availableRefundAmount")
    private BigDecimal availableRefundAmount;

    // Factory method to create from Payment entity
    public static PaymentResponse fromEntity(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setPaymentReference(payment.getPaymentReference());
        response.setAmount(payment.getAmount());
        response.setCurrencyCode(payment.getCurrencyCode());
        response.setDescription(payment.getDescription());
        response.setStatus(payment.getStatus());
        response.setMerchantReference(payment.getMerchantReference());
        response.setCallbackUrl(payment.getCallbackUrl());
        response.setSuccessUrl(payment.getSuccessUrl());
        response.setFailureUrl(payment.getFailureUrl());
        response.setMetadata(payment.getMetadata());
        response.setRiskScore(payment.getRiskScore());
        response.setProcessedAt(payment.getProcessedAt());
        response.setExpiresAt(payment.getExpiresAt());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        
        // Set computed fields
        response.setIsExpired(payment.isExpired());
        response.setCanBeProcessed(payment.canBeProcessed());
        response.setCanBeRefunded(payment.canBeRefunded());
        response.setCanBeCancelled(payment.canBeCancelled());
        
        return response;
    }

    // Factory method with related entities
    public static PaymentResponse fromEntityWithDetails(Payment payment, boolean includeAccount, 
                                                       boolean includePaymentMethod, boolean includeTransactions) {
        PaymentResponse response = fromEntity(payment);
        
        if (includeAccount && payment.getAccount() != null) {
            response.setAccount(AccountResponse.fromEntity(payment.getAccount()));
        }
        
        if (includePaymentMethod && payment.getPaymentMethod() != null) {
            response.setPaymentMethod(PaymentMethodResponse.fromEntity(payment.getPaymentMethod()));
        }
        
        if (includeTransactions && payment.getTransactions() != null) {
            response.setTransactions(
                payment.getTransactions().stream()
                    .map(TransactionResponse::fromEntity)
                    .toList()
            );
            
            // Calculate transaction amounts
            BigDecimal totalAmount = payment.getTransactions().stream()
                .map(t -> t.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            response.setTotalTransactionAmount(totalAmount);
            
            BigDecimal successfulAmount = payment.getTransactions().stream()
                .filter(t -> t.getStatus() == Payment.PaymentStatus.COMPLETED)
                .map(t -> t.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            response.setSuccessfulTransactionAmount(successfulAmount);
            
            BigDecimal refundedAmount = payment.getTransactions().stream()
                .filter(t -> t.getType() == com.enterprise.payment.entity.Transaction.TransactionType.REFUND 
                           && t.getStatus() == Payment.PaymentStatus.COMPLETED)
                .map(t -> t.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            response.setRefundedAmount(refundedAmount);
            
            // Available refund amount = successful amount - refunded amount
            response.setAvailableRefundAmount(successfulAmount.subtract(refundedAmount));
        }
        
        return response;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaymentStatusHistoryResponse {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("previousStatus")
        private Payment.PaymentStatus previousStatus;

        @JsonProperty("newStatus")
        private Payment.PaymentStatus newStatus;

        @JsonProperty("reason")
        private String reason;

        @JsonProperty("changedBy")
        private String changedBy;

        @JsonProperty("changedAt")
        private OffsetDateTime changedAt;

        @JsonProperty("metadata")
        private Map<String, Object> metadata;
    }
}