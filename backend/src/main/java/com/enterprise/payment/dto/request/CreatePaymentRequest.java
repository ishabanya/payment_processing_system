package com.enterprise.payment.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Amount must have at most 2 decimal places")
    private BigDecimal amount;

    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be 3 uppercase letters")
    private String currencyCode = "USD";

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @JsonProperty("paymentMethodId")
    private Long paymentMethodId;

    @JsonProperty("accountId")
    private Long accountId;

    @Size(max = 255, message = "Merchant reference cannot exceed 255 characters")
    private String merchantReference;

    @Size(max = 500, message = "Callback URL cannot exceed 500 characters")
    @Pattern(regexp = "^https?://.*", message = "Callback URL must be a valid HTTP/HTTPS URL")
    private String callbackUrl;

    @Size(max = 500, message = "Success URL cannot exceed 500 characters")
    @Pattern(regexp = "^https?://.*", message = "Success URL must be a valid HTTP/HTTPS URL")
    private String successUrl;

    @Size(max = 500, message = "Failure URL cannot exceed 500 characters")
    @Pattern(regexp = "^https?://.*", message = "Failure URL must be a valid HTTP/HTTPS URL")
    private String failureUrl;

    private Map<String, Object> metadata;

    @Future(message = "Expiry date must be in the future")
    private OffsetDateTime expiresAt;

    @DecimalMin(value = "0.0", message = "Risk score must be non-negative")
    @DecimalMax(value = "100.0", message = "Risk score must not exceed 100")
    private BigDecimal riskScore;

    // Validation method to ensure at least one URL is provided for redirects
    public boolean hasValidUrls() {
        return (callbackUrl != null && !callbackUrl.trim().isEmpty()) ||
               (successUrl != null && !successUrl.trim().isEmpty()) ||
               (failureUrl != null && !failureUrl.trim().isEmpty());
    }
}