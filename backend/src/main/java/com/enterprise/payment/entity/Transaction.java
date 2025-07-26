package com.enterprise.payment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transactions_transaction_reference", columnList = "transactionReference"),
    @Index(name = "idx_transactions_payment_id", columnList = "payment_id"),
    @Index(name = "idx_transactions_type", columnList = "type"),
    @Index(name = "idx_transactions_status", columnList = "status"),
    @Index(name = "idx_transactions_created_at", columnList = "createdAt"),
    @Index(name = "idx_transactions_processed_at", columnList = "processedAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_reference", unique = true, nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String transactionReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    @NotNull
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false, length = 3)
    @NotBlank
    @Size(min = 3, max = 3)
    private String currencyCode = "USD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private Payment.PaymentStatus status;

    @Column(name = "description", length = 500)
    @Size(max = 500)
    private String description;

    @Column(name = "gateway_transaction_id")
    @Size(max = 255)
    private String gatewayTransactionId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "gateway_response", columnDefinition = "jsonb")
    private Map<String, Object> gatewayResponse;

    @Column(name = "processing_fee", precision = 19, scale = 2)
    @DecimalMin(value = "0.0")
    private BigDecimal processingFee = BigDecimal.ZERO;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    public enum TransactionType {
        PAYMENT, REFUND, CHARGEBACK, ADJUSTMENT
    }

    public BigDecimal getNetAmount() {
        return amount.subtract(processingFee != null ? processingFee : BigDecimal.ZERO);
    }

    public boolean isSuccessful() {
        return status == Payment.PaymentStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == Payment.PaymentStatus.FAILED;
    }

    public boolean isPending() {
        return status == Payment.PaymentStatus.PENDING || status == Payment.PaymentStatus.PROCESSING;
    }
}