package com.enterprise.payment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "webhooks", indexes = {
    @Index(name = "idx_webhooks_account_id", columnList = "account_id"),
    @Index(name = "idx_webhooks_is_active", columnList = "isActive")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Webhook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    @NotNull
    private Account account;

    @Column(nullable = false, length = 500)
    @NotBlank
    @Size(max = 500)
    private String url;

    @ElementCollection
    @CollectionTable(name = "webhook_events", joinColumns = @JoinColumn(name = "webhook_id"))
    @Column(name = "event")
    private List<String> events;

    @Column(nullable = false)
    @NotBlank
    @Size(max = 255)
    private String secret;

    @Column(name = "is_active", nullable = false)
    @NotNull
    private Boolean isActive = true;

    @Column(name = "retry_attempts")
    private Integer retryAttempts = 3;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "webhook", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WebhookDelivery> deliveries;

    public boolean supportsEvent(String event) {
        return events != null && events.contains(event);
    }
}