package com.enterprise.payment.dto.response;

import com.enterprise.payment.entity.Payment;
import com.enterprise.payment.entity.Transaction;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("transactionReference")
    private String transactionReference;

    @JsonProperty("type")
    private Transaction.TransactionType type;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("currencyCode")
    private String currencyCode;

    @JsonProperty("status")
    private Payment.PaymentStatus status;

    @JsonProperty("gatewayTransactionId")
    private String gatewayTransactionId;

    @JsonProperty("gatewayResponse")
    private Map<String, Object> gatewayResponse;

    @JsonProperty("processingFee")
    private BigDecimal processingFee;

    @JsonProperty("netAmount")
    private BigDecimal netAmount;

    @JsonProperty("processedAt")
    private OffsetDateTime processedAt;

    @JsonProperty("createdAt")
    private OffsetDateTime createdAt;

    @JsonProperty("updatedAt")
    private OffsetDateTime updatedAt;

    // Related payment information (minimal)
    @JsonProperty("payment")
    private PaymentSummary payment;

    // Computed fields
    @JsonProperty("isSuccessful")
    private Boolean isSuccessful;

    @JsonProperty("isFailed")
    private Boolean isFailed;

    @JsonProperty("isPending")
    private Boolean isPending;

    @JsonProperty("processingTimeMs")
    private Long processingTimeMs;

    // Factory method to create from Transaction entity
    public static TransactionResponse fromEntity(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setTransactionReference(transaction.getTransactionReference());
        response.setType(transaction.getType());
        response.setAmount(transaction.getAmount());
        response.setCurrencyCode(transaction.getCurrencyCode());
        response.setStatus(transaction.getStatus());
        response.setGatewayTransactionId(transaction.getGatewayTransactionId());
        response.setGatewayResponse(transaction.getGatewayResponse());
        response.setProcessingFee(transaction.getProcessingFee());
        response.setNetAmount(transaction.getNetAmount());
        response.setProcessedAt(transaction.getProcessedAt());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setUpdatedAt(transaction.getUpdatedAt());
        
        // Set computed fields
        response.setIsSuccessful(transaction.isSuccessful());
        response.setIsFailed(transaction.isFailed());
        response.setIsPending(transaction.isPending());
        
        // Calculate processing time if processed
        if (transaction.getProcessedAt() != null && transaction.getCreatedAt() != null) {
            long processingTime = java.time.Duration.between(
                transaction.getCreatedAt(), 
                transaction.getProcessedAt()
            ).toMillis();
            response.setProcessingTimeMs(processingTime);
        }
        
        return response;
    }

    // Factory method with payment information
    public static TransactionResponse fromEntityWithPayment(Transaction transaction) {
        TransactionResponse response = fromEntity(transaction);
        
        if (transaction.getPayment() != null) {
            PaymentSummary paymentSummary = new PaymentSummary();
            paymentSummary.setId(transaction.getPayment().getId());
            paymentSummary.setPaymentReference(transaction.getPayment().getPaymentReference());
            paymentSummary.setAmount(transaction.getPayment().getAmount());
            paymentSummary.setCurrencyCode(transaction.getPayment().getCurrencyCode());
            paymentSummary.setStatus(transaction.getPayment().getStatus());
            paymentSummary.setMerchantReference(transaction.getPayment().getMerchantReference());
            response.setPayment(paymentSummary);
        }
        
        return response;
    }

    // Factory method for minimal response (without sensitive gateway data)
    public static TransactionResponse fromEntityMinimal(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setTransactionReference(transaction.getTransactionReference());
        response.setType(transaction.getType());
        response.setAmount(transaction.getAmount());
        response.setCurrencyCode(transaction.getCurrencyCode());
        response.setStatus(transaction.getStatus());
        response.setProcessingFee(transaction.getProcessingFee());
        response.setNetAmount(transaction.getNetAmount());
        response.setProcessedAt(transaction.getProcessedAt());
        response.setCreatedAt(transaction.getCreatedAt());
        
        // Set computed fields
        response.setIsSuccessful(transaction.isSuccessful());
        response.setIsFailed(transaction.isFailed());
        response.setIsPending(transaction.isPending());
        
        return response;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaymentSummary {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("paymentReference")
        private String paymentReference;

        @JsonProperty("amount")
        private BigDecimal amount;

        @JsonProperty("currencyCode")
        private String currencyCode;

        @JsonProperty("status")
        private Payment.PaymentStatus status;

        @JsonProperty("merchantReference")
        private String merchantReference;
    }
}