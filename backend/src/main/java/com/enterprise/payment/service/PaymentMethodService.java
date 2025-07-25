package com.enterprise.payment.service;

import com.enterprise.payment.dto.request.CreatePaymentMethodRequest;
import com.enterprise.payment.dto.response.PaymentMethodResponse;
import com.enterprise.payment.entity.Account;
import com.enterprise.payment.entity.PaymentMethod;
import com.enterprise.payment.exception.AccountNotFoundException;
import com.enterprise.payment.exception.PaymentNotFoundException;
import com.enterprise.payment.exception.ValidationException;
import com.enterprise.payment.repository.AccountRepository;
import com.enterprise.payment.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing payment methods including encryption/decryption of sensitive data
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentMethodService extends BaseService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final AccountRepository accountRepository;
    private final EncryptionService encryptionService;

    /**
     * Create a new payment method
     */
    @Transactional
    public PaymentMethodResponse createPaymentMethod(CreatePaymentMethodRequest request) {
        logMethodEntry("createPaymentMethod", request);
        
        validateCreatePaymentMethodRequest(request);
        
        Account account = accountRepository.findById(request.getAccountId())
            .orElseThrow(() -> new AccountNotFoundException(request.getAccountId()));
            
        validateAccountForPaymentMethod(account);
        
        PaymentMethod paymentMethod = createPaymentMethodEntity(request, account);
        paymentMethod = paymentMethodRepository.save(paymentMethod);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("type", paymentMethod.getType());
        metadata.put("provider", paymentMethod.getProvider());
        metadata.put("accountId", account.getId());
        
        auditLog("PAYMENT_METHOD_CREATED", "PAYMENT_METHOD", paymentMethod.getId(), 
                "Payment method created for account: " + account.getAccountNumber(), metadata);
        
        PaymentMethodResponse response = mapToPaymentMethodResponse(paymentMethod);
        logMethodExit("createPaymentMethod", response);
        return response;
    }

    /**
     * Get payment method by ID
     */
    @Cacheable(value = "payment-methods", key = "#paymentMethodId")
    public PaymentMethodResponse getPaymentMethodById(Long paymentMethodId) {
        logMethodEntry("getPaymentMethodById", paymentMethodId);
        
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentMethodId));
            
        PaymentMethodResponse response = mapToPaymentMethodResponse(paymentMethod);
        logMethodExit("getPaymentMethodById", response);
        return response;
    }

    /**
     * Get payment methods for account
     */
    public Page<PaymentMethodResponse> getPaymentMethodsForAccount(Long accountId, Pageable pageable) {
        logMethodEntry("getPaymentMethodsForAccount", accountId, pageable);
        
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException(accountId));
            
        Page<PaymentMethod> paymentMethods = paymentMethodRepository
            .findByAccountAndIsActiveOrderByCreatedAtDesc(account, true, pageable);
        Page<PaymentMethodResponse> response = paymentMethods.map(this::mapToPaymentMethodResponse);
        
        logMethodExit("getPaymentMethodsForAccount", response.getTotalElements());
        return response;
    }

    /**
     * Get payment methods by type
     */
    public Page<PaymentMethodResponse> getPaymentMethodsByType(PaymentMethod.PaymentMethodType type, 
                                                             Pageable pageable) {
        logMethodEntry("getPaymentMethodsByType", type, pageable);
        
        Page<PaymentMethod> paymentMethods = paymentMethodRepository
            .findByTypeAndIsActiveOrderByCreatedAtDesc(type, true, pageable);
        Page<PaymentMethodResponse> response = paymentMethods.map(this::mapToPaymentMethodResponse);
        
        logMethodExit("getPaymentMethodsByType", response.getTotalElements());
        return response;
    }

    /**
     * Update payment method status
     */
    @Transactional
    @CacheEvict(value = "payment-methods", key = "#paymentMethodId")
    public PaymentMethodResponse updatePaymentMethodStatus(Long paymentMethodId, boolean isActive) {
        logMethodEntry("updatePaymentMethodStatus", paymentMethodId, isActive);
        
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentMethodId));
            
        Boolean oldStatus = paymentMethod.getIsActive();
        paymentMethod.setIsActive(isActive);
        paymentMethod.setUpdatedAt(OffsetDateTime.now());
        
        paymentMethod = paymentMethodRepository.save(paymentMethod);
        
        auditLog("PAYMENT_METHOD_STATUS_UPDATED", "PAYMENT_METHOD", paymentMethod.getId(), 
                String.format("Status updated from %s to %s", oldStatus, isActive));
        
        log.info("Payment method status updated: {} from {} to {}", 
                paymentMethodId, oldStatus, isActive);
        
        PaymentMethodResponse response = mapToPaymentMethodResponse(paymentMethod);
        logMethodExit("updatePaymentMethodStatus", response);
        return response;
    }

    /**
     * Delete payment method (soft delete)
     */
    @Transactional
    @CacheEvict(value = "payment-methods", key = "#paymentMethodId")
    public void deletePaymentMethod(Long paymentMethodId) {
        logMethodEntry("deletePaymentMethod", paymentMethodId);
        
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentMethodId));
            
        paymentMethod.setIsActive(false);
        paymentMethod.setUpdatedAt(OffsetDateTime.now());
        
        paymentMethodRepository.save(paymentMethod);
        
        auditLog("PAYMENT_METHOD_DELETED", "PAYMENT_METHOD", paymentMethod.getId(), 
                "Payment method soft deleted");
        
        log.info("Payment method soft deleted: {}", paymentMethodId);
        logMethodExit("deletePaymentMethod");
    }

    /**
     * Validate payment method for processing
     */
    public boolean validatePaymentMethod(Long paymentMethodId) {
        logMethodEntry("validatePaymentMethod", paymentMethodId);
        
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentMethodId));
            
        boolean isValid = paymentMethod.getIsActive() && 
                         (paymentMethod.getExpiresAt() == null || 
                          paymentMethod.getExpiresAt().isAfter(OffsetDateTime.now()));
        
        logMethodExit("validatePaymentMethod", isValid);
        return isValid;
    }

    /**
     * Get decrypted payment method details (for authorized operations only)
     */
    @Transactional
    public Map<String, String> getDecryptedPaymentMethodDetails(Long paymentMethodId) {
        logMethodEntry("getDecryptedPaymentMethodDetails", paymentMethodId);
        
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentMethodId));
            
        Map<String, String> decryptedDetails = new HashMap<>();
        
        if (paymentMethod.getEncryptedDetails() != null) {
            String decryptedData = encryptionService.decryptPaymentData(paymentMethod.getEncryptedDetails());
            // Parse the decrypted data (this would depend on your encryption format)
            decryptedDetails.put("details", decryptedData);
        }
        
        auditLog("PAYMENT_METHOD_DETAILS_ACCESSED", "PAYMENT_METHOD", paymentMethod.getId(), 
                "Payment method details decrypted and accessed");
        
        log.info("Payment method details decrypted for method: {}", paymentMethodId);
        logMethodExit("getDecryptedPaymentMethodDetails", "details_decrypted");
        return decryptedDetails;
    }

    /**
     * Update payment method details with encryption
     */
    @Transactional
    @CacheEvict(value = "payment-methods", key = "#paymentMethodId")
    public PaymentMethodResponse updatePaymentMethodDetails(Long paymentMethodId, 
                                                           Map<String, String> sensitiveDetails) {
        logMethodEntry("updatePaymentMethodDetails", paymentMethodId, "***");
        
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentMethodId));
            
        // Encrypt sensitive details
        String encryptedDetails = encryptionService.encryptPaymentData(
            sensitiveDetails.toString()); // This would need proper JSON serialization
        
        paymentMethod.setEncryptedDetails(encryptedDetails);
        paymentMethod.setUpdatedAt(OffsetDateTime.now());
        
        paymentMethod = paymentMethodRepository.save(paymentMethod);
        
        auditLog("PAYMENT_METHOD_DETAILS_UPDATED", "PAYMENT_METHOD", paymentMethod.getId(), 
                "Payment method details updated with encryption");
        
        log.info("Payment method details updated with encryption: {}", paymentMethodId);
        
        PaymentMethodResponse response = mapToPaymentMethodResponse(paymentMethod);
        logMethodExit("updatePaymentMethodDetails", response);
        return response;
    }

    /**
     * Get payment method statistics for account
     */
    @Cacheable(value = "payment-method-stats", key = "#accountId")
    public Map<String, Object> getPaymentMethodStatistics(Long accountId) {
        logMethodEntry("getPaymentMethodStatistics", accountId);
        
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException(accountId));
            
        Map<String, Object> stats = new HashMap<>();
        
        // Get total count
        Long totalCount = paymentMethodRepository.countByAccountAndIsActive(account, true);
        stats.put("totalPaymentMethods", totalCount);
        
        // Get count by type
        for (PaymentMethod.PaymentMethodType type : PaymentMethod.PaymentMethodType.values()) {
            Long count = paymentMethodRepository.countByAccountAndTypeAndIsActive(account, type, true);
            stats.put(type.name().toLowerCase() + "Methods", count);
        }
        
        // Get recent additions (last 30 days)
        OffsetDateTime thirtyDaysAgo = OffsetDateTime.now().minusDays(30);
        Long recentCount = paymentMethodRepository
            .countByAccountAndIsActiveAndCreatedAtAfter(account, true, thirtyDaysAgo);
        stats.put("recentPaymentMethods", recentCount);
        
        logMethodExit("getPaymentMethodStatistics", stats);
        return stats;
    }

    // Private helper methods

    private void validateCreatePaymentMethodRequest(CreatePaymentMethodRequest request) {
        validateRequired(request.getAccountId(), "accountId");
        validateRequired(request.getType(), "type");
        validateRequired(request.getProvider(), "provider");
        
        if (request.getType() == PaymentMethod.PaymentMethodType.CARD) {
            validateRequired(request.getCardNumber(), "cardNumber");
            validateRequired(request.getExpiryMonth(), "expiryMonth");
            validateRequired(request.getExpiryYear(), "expiryYear");
            validateRequired(request.getCardholderName(), "cardholderName");
            
            validateCardNumber(request.getCardNumber());
            validateExpiryDate(request.getExpiryMonth(), request.getExpiryYear());
        }
    }

    private void validateAccountForPaymentMethod(Account account) {
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new ValidationException("Cannot add payment method to inactive account");
        }
    }

    private PaymentMethod createPaymentMethodEntity(CreatePaymentMethodRequest request, Account account) {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setAccount(account);
        paymentMethod.setType(request.getType());
        paymentMethod.setProvider(request.getProvider());
        paymentMethod.setIsActive(true);
        
        // Handle sensitive data encryption
        if (request.getType() == PaymentMethod.PaymentMethodType.CARD) {
            Map<String, String> sensitiveData = new HashMap<>();
            sensitiveData.put("cardNumber", request.getCardNumber());
            sensitiveData.put("cardholderName", request.getCardholderName());
            sensitiveData.put("cvv", request.getCvv());
            
            String encryptedDetails = encryptionService.encryptPaymentData(sensitiveData.toString());
            paymentMethod.setEncryptedDetails(encryptedDetails);
            
            // Store masked card number for display
            paymentMethod.setMaskedDetails(maskCardNumber(request.getCardNumber()));
            
            // Set expiry date
            paymentMethod.setExpiresAt(OffsetDateTime.of(
                request.getExpiryYear(), request.getExpiryMonth(), 1, 0, 0, 0, 0,
                OffsetDateTime.now().getOffset()).plusMonths(1).minusDays(1));
        }
        
        paymentMethod.setCreatedAt(OffsetDateTime.now());
        paymentMethod.setUpdatedAt(OffsetDateTime.now());
        
        return paymentMethod;
    }

    private void validateCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 13 || cardNumber.length() > 19) {
            throw new ValidationException("Invalid card number length");
        }
        
        if (!cardNumber.matches("\\d+")) {
            throw new ValidationException("Card number must contain only digits");
        }
        
        // Luhn algorithm validation
        if (!isValidLuhn(cardNumber)) {
            throw new ValidationException("Invalid card number");
        }
    }

    private void validateExpiryDate(Integer month, Integer year) {
        if (month < 1 || month > 12) {
            throw new ValidationException("Invalid expiry month");
        }
        
        if (year < OffsetDateTime.now().getYear()) {
            throw new ValidationException("Card has expired");
        }
        
        if (year == OffsetDateTime.now().getYear() && 
            month < OffsetDateTime.now().getMonthValue()) {
            throw new ValidationException("Card has expired");
        }
    }

    private boolean isValidLuhn(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            
            sum += n;
            alternate = !alternate;
        }
        
        return (sum % 10 == 0);
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }

    private PaymentMethodResponse mapToPaymentMethodResponse(PaymentMethod paymentMethod) {
        PaymentMethodResponse response = new PaymentMethodResponse();
        response.setId(paymentMethod.getId());
        response.setAccountId(paymentMethod.getAccount().getId());
        response.setType(paymentMethod.getType().toString());
        response.setProvider(paymentMethod.getProvider());
        response.setMaskedDetails(paymentMethod.getMaskedDetails());
        response.setIsActive(paymentMethod.getIsActive());
        response.setExpiresAt(paymentMethod.getExpiresAt());
        response.setCreatedAt(paymentMethod.getCreatedAt());
        response.setUpdatedAt(paymentMethod.getUpdatedAt());
        return response;
    }
}