package com.enterprise.payment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "webhook_deliveries", indexes = {
    @Index(name = "idx_webhook_deliveries_webhook_id", columnList = "webhook_id"),
    @Index(name = "idx_webhook_deliveries_payment_id", columnList = "payment_id"),
    @Index(name = "idx_webhook_deliveries_event", columnList = "event"),
    @Index(name = "idx_webhook_deliveries_created_at", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WebhookDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "webhook_id", nullable = false)
    @NotNull
    private Webhook webhook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Column(nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String event;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "delivered_at")
    private OffsetDateTime deliveredAt;

    @Column(nullable = false)
    @NotNull
    private Integer attempts = 0;

    @Column(name = "next_retry_at")
    private OffsetDateTime nextRetryAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;

    public boolean isDelivered() {
        return deliveredAt != null && responseStatus != null && responseStatus >= 200 && responseStatus < 300;
    }

    public boolean shouldRetry() {
        return !isDelivered() && attempts < webhook.getRetryAttempts() && 
               (nextRetryAt == null || nextRetryAt.isBefore(OffsetDateTime.now()));
    }

    public void markAsDelivered(int status, String responseBody) {
        this.deliveredAt = OffsetDateTime.now();
        this.responseStatus = status;
        this.responseBody = responseBody;
    }

    public void incrementAttempts() {
        this.attempts++;
        // Exponential backoff: 1min, 5min, 25min
        int delayMinutes = (int) Math.pow(5, attempts - 1);
        this.nextRetryAt = OffsetDateTime.now().plusMinutes(delayMinutes);
    }
}