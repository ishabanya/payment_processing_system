package com.enterprise.payment.dto.request;

import com.enterprise.payment.entity.Payment;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePaymentStatusRequest {

    @NotNull(message = "Payment status is required")
    @JsonProperty("status")
    private Payment.PaymentStatus status;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    @Size(max = 255, message = "Gateway transaction ID cannot exceed 255 characters")
    private String gatewayTransactionId;

    // Additional metadata from payment gateway
    private java.util.Map<String, Object> gatewayResponse;

    // Validation method to ensure reason is provided for certain status changes
    public boolean requiresReason() {
        return status == Payment.PaymentStatus.FAILED || 
               status == Payment.PaymentStatus.CANCELLED ||
               status == Payment.PaymentStatus.REFUNDED;
    }

    public boolean isValidStatusTransition(Payment.PaymentStatus currentStatus) {
        if (currentStatus == null) {
            return status == Payment.PaymentStatus.PENDING;
        }

        return switch (currentStatus) {
            case PENDING -> status == Payment.PaymentStatus.PROCESSING || 
                          status == Payment.PaymentStatus.CANCELLED ||
                          status == Payment.PaymentStatus.FAILED;
            case PROCESSING -> status == Payment.PaymentStatus.COMPLETED || 
                              status == Payment.PaymentStatus.FAILED;
            case COMPLETED -> status == Payment.PaymentStatus.REFUNDED;
            case FAILED, CANCELLED, REFUNDED -> false; // Terminal states
        };
    }
}