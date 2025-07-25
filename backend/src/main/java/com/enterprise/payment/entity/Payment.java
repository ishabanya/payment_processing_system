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
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payments_payment_reference", columnList = "paymentReference"),
    @Index(name = "idx_payments_account_id", columnList = "account_id"),
    @Index(name = "idx_payments_status", columnList = "status"),
    @Index(name = "idx_payments_created_at", columnList = "createdAt"),
    @Index(name = "idx_payments_processed_at", columnList = "processedAt"),
    @Index(name = "idx_payments_merchant_reference", columnList = "merchantReference")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_reference", unique = true, nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String paymentReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    @NotNull
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    @Column(nullable = false, precision = 19, scale = 2)
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false, length = 3)
    @NotBlank
    @Size(min = 3, max = 3)
    private String currencyCode = "USD";

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "merchant_reference")
    @Size(max = 255)
    private String merchantReference;

    @Column(name = "callback_url", length = 500)
    @Size(max = 500)
    private String callbackUrl;

    @Column(name = "success_url", length = 500)
    @Size(max = 500)
    private String successUrl;

    @Column(name = "failure_url", length = 500)
    @Size(max = 500)
    private String failureUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "risk_score", precision = 5, scale = 2)
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    private BigDecimal riskScore;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PaymentStatusHistory> statusHistory;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WebhookDelivery> webhookDeliveries;

    public enum PaymentStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REFUNDED
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(OffsetDateTime.now());
    }

    public boolean canBeProcessed() {
        return status == PaymentStatus.PENDING && !isExpired();
    }

    public boolean canBeRefunded() {
        return status == PaymentStatus.COMPLETED;
    }

    public boolean canBeCancelled() {
        return status == PaymentStatus.PENDING || status == PaymentStatus.PROCESSING;
    }
}