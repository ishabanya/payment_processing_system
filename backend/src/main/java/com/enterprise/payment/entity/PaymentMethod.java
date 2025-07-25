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
@Table(name = "payment_methods", indexes = {
    @Index(name = "idx_payment_methods_account_id", columnList = "account_id"),
    @Index(name = "idx_payment_methods_type", columnList = "type"),
    @Index(name = "idx_payment_methods_is_active", columnList = "isActive")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    @NotNull
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private PaymentMethodType type;

    @Column(nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String provider;

    @Column(name = "encrypted_details", nullable = false, columnDefinition = "TEXT")
    @NotBlank
    private String encryptedDetails;

    @Column(name = "last_four_digits", length = 4)
    @Size(max = 4)
    private String lastFourDigits;

    @Column(name = "expiry_month")
    @Min(1)
    @Max(12)
    private Integer expiryMonth;

    @Column(name = "expiry_year")
    @Min(2024)
    private Integer expiryYear;

    @Column(name = "is_default", nullable = false)
    @NotNull
    private Boolean isDefault = false;

    @Column(name = "is_active", nullable = false)
    @NotNull
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "paymentMethod", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments;

    public enum PaymentMethodType {
        CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, DIGITAL_WALLET, CRYPTO
    }

    public boolean isExpired() {
        if (expiryMonth == null || expiryYear == null) {
            return false;
        }
        OffsetDateTime now = OffsetDateTime.now();
        return now.getYear() > expiryYear || 
               (now.getYear() == expiryYear && now.getMonthValue() > expiryMonth);
    }

    public String getMaskedDetails() {
        if (lastFourDigits != null) {
            return "**** **** **** " + lastFourDigits;
        }
        return "****";
    }
}