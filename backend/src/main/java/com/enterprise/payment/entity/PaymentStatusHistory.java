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
@Table(name = "payment_status_history", indexes = {
    @Index(name = "idx_payment_status_history_payment_id", columnList = "payment_id"),
    @Index(name = "idx_payment_status_history_changed_at", columnList = "changedAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PaymentStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    @NotNull
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private Payment.PaymentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    @NotNull
    private Payment.PaymentStatus toStatus;

    @Column(length = 500)
    @Size(max = 500)
    private String reason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> details;

    @Column(name = "changed_by")
    @Size(max = 255)
    private String changedBy;

    @Column(name = "changed_at", nullable = false, updatable = false)
    @CreationTimestamp
    private OffsetDateTime changedAt;

    public static PaymentStatusHistory create(Payment payment, Payment.PaymentStatus fromStatus, 
                                            Payment.PaymentStatus toStatus, String reason, String changedBy) {
        PaymentStatusHistory history = new PaymentStatusHistory();
        history.setPayment(payment);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setReason(reason);
        history.setChangedBy(changedBy);
        return history;
    }
}