package com.enterprise.payment.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundPaymentRequest {

    @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Refund amount must have at most 2 decimal places")
    private BigDecimal amount;

    @NotBlank(message = "Refund reason is required")
    @Size(max = 500, message = "Refund reason cannot exceed 500 characters")
    private String reason;

    @Size(max = 255, message = "Reference cannot exceed 255 characters")
    private String reference;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    private Map<String, Object> metadata;

    // Indicates if this is a partial refund
    private Boolean isPartialRefund = false;

    // For scheduled refunds
    private java.time.OffsetDateTime scheduledAt;

    // Business validation methods
    public boolean isValidRefund() {
        return reason != null && !reason.trim().isEmpty() &&
               (amount == null || amount.compareTo(BigDecimal.ZERO) > 0);
    }

    public boolean isFullRefund() {
        return amount == null && !Boolean.TRUE.equals(isPartialRefund);
    }

    public boolean isScheduledRefund() {
        return scheduledAt != null && scheduledAt.isAfter(java.time.OffsetDateTime.now());
    }

    public RefundType getRefundType() {
        if (isFullRefund()) {
            return RefundType.FULL;
        } else if (Boolean.TRUE.equals(isPartialRefund)) {
            return RefundType.PARTIAL;
        }
        return RefundType.AMOUNT_BASED;
    }

    public enum RefundType {
        FULL,      // Refund entire payment amount
        PARTIAL,   // Partial refund based on amount
        AMOUNT_BASED // Specific amount refund
    }
}