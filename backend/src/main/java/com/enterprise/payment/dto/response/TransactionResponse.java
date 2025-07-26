package com.enterprise.payment.dto.response;

import com.enterprise.payment.entity.Payment;
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

    @JsonProperty("paymentReference")
    private String paymentReference;

    @JsonProperty("type")
    private String type;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("currencyCode")
    private String currencyCode;

    @JsonProperty("description")
    private String description;

    @JsonProperty("status")
    private String status;

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
    public static TransactionResponse fromEntity(com.enterprise.payment.entity.Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setTransactionReference(transaction.getTransactionReference());
        response.setPaymentReference(transaction.getPayment().getPaymentReference());
        response.setType(transaction.getType().toString());
        response.setAmount(transaction.getAmount());
        response.setCurrencyCode(transaction.getCurrencyCode());
        response.setDescription(transaction.getDescription());
        response.setStatus(transaction.getStatus().toString());
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