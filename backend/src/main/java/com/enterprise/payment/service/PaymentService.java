package com.enterprise.payment.service;

import com.enterprise.payment.dto.request.CreatePaymentRequest;
import com.enterprise.payment.dto.request.RefundPaymentRequest;
import com.enterprise.payment.dto.request.UpdatePaymentStatusRequest;
import com.enterprise.payment.dto.response.PaymentResponse;
import com.enterprise.payment.entity.Account;
import com.enterprise.payment.entity.Payment;
import com.enterprise.payment.entity.PaymentMethod;
import com.enterprise.payment.entity.PaymentStatusHistory;
import com.enterprise.payment.exception.AccountNotFoundException;
import com.enterprise.payment.exception.InsufficientFundsException;
import com.enterprise.payment.exception.PaymentNotFoundException;
import com.enterprise.payment.exception.PaymentProcessingException;
import com.enterprise.payment.exception.ValidationException;
import com.enterprise.payment.repository.AccountRepository;
import com.enterprise.payment.repository.PaymentMethodRepository;
import com.enterprise.payment.repository.PaymentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing payment lifecycle including creation, processing, and refunds
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService extends BaseService {

    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final RiskAssessmentService riskAssessmentService;
    private final NotificationService notificationService;
    private final WebhookService webhookService;

    /**
     * Create a new payment
     */
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        logMethodEntry("createPayment", request);
        
        validateCreatePaymentRequest(request);
        
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
            .orElseThrow(() -> new AccountNotFoundException(request.getAccountNumber()));
            
        validateAccountStatus(account);
        
        PaymentMethod paymentMethod = null;
        if (request.getPaymentMethodId() != null) {
            paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new ValidationException("Invalid payment method ID"));
        }
        
        Payment payment = createPaymentEntity(request, account, paymentMethod);
        
        // Perform risk assessment
        BigDecimal riskScore = riskAssessmentService.assessPaymentRisk(payment);
        payment.setRiskScore(riskScore);
        
        // Check if payment should be auto-approved based on risk score
        if (riskScore.compareTo(BigDecimal.valueOf(70)) > 0) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            auditLog("PAYMENT_REJECTED", "PAYMENT", payment.getPaymentReference(), 
                    "Payment rejected due to high risk score: " + riskScore);
        }
        
        payment = paymentRepository.save(payment);
        
        // Create status history
        createStatusHistory(payment, Payment.PaymentStatus.PENDING, "Payment created");
        
        auditLog("PAYMENT_CREATED", "PAYMENT", payment.getPaymentReference(), 
                "Payment created for amount: " + payment.getAmount());
        
        // Async processing
        processPaymentAsync(payment.getId());
        
        PaymentResponse response = mapToPaymentResponse(payment);
        logMethodExit("createPayment", response);
        return response;
    }

    /**
     * Process payment asynchronously
     */
    @Async
    @CircuitBreaker(name = "payment-processing", fallbackMethod = "processPaymentFallback")
    @Retry(name = "payment-processing")
    public CompletableFuture<Void> processPaymentAsync(Long paymentId) {
        return CompletableFuture.runAsync(() -> {
            try {
                processPayment(paymentId);
            } catch (Exception e) {
                log.error("Failed to process payment asynchronously: {}", paymentId, e);
                handlePaymentProcessingFailure(paymentId, e.getMessage());
            }
        });
    }

    /**
     * Process payment synchronously
     */
    @Transactional
    public void processPayment(Long paymentId) {
        logMethodEntry("processPayment", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));
            
        if (!payment.canBeProcessed()) {
            throw new PaymentProcessingException("Payment cannot be processed in current state: " + payment.getStatus());
        }
        
        try {
            // Update status to processing
            updatePaymentStatus(payment, Payment.PaymentStatus.PROCESSING, "Payment processing started");
            
            // Check account balance if needed
            if (payment.getAccount().getBalance().compareTo(payment.getAmount()) < 0) {
                throw new InsufficientFundsException(payment.getAmount(), payment.getAccount().getBalance());
            }
            
            // Process through gateway
            boolean processed = paymentGatewayService.processPayment(payment);
            
            if (processed) {
                updatePaymentStatus(payment, Payment.PaymentStatus.COMPLETED, "Payment processed successfully");
                payment.setProcessedAt(OffsetDateTime.now());
                
                // Update account balance
                Account account = payment.getAccount();
                account.setBalance(account.getBalance().subtract(payment.getAmount()));
                accountRepository.save(account);
                
                auditLog("PAYMENT_PROCESSED", "PAYMENT", payment.getPaymentReference(), 
                        "Payment processed successfully");
                
                // Send notifications
                notificationService.sendPaymentSuccessNotification(payment);
                webhookService.sendPaymentWebhook(payment, "payment.completed");
            } else {
                updatePaymentStatus(payment, Payment.PaymentStatus.FAILED, "Payment processing failed");
                auditLog("PAYMENT_FAILED", "PAYMENT", payment.getPaymentReference(), 
                        "Payment processing failed");
            }
            
        } catch (Exception e) {
            log.error("Payment processing failed for payment: {}", paymentId, e);
            updatePaymentStatus(payment, Payment.PaymentStatus.FAILED, "Payment processing error: " + e.getMessage());
            throw new PaymentProcessingException("Payment processing failed", e);
        }
        
        logMethodExit("processPayment");
    }

    /**
     * Refund a payment
     */
    @Transactional
    public PaymentResponse refundPayment(String paymentReference, RefundPaymentRequest request) {
        logMethodEntry("refundPayment", paymentReference, request);
        
        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
            .orElseThrow(() -> new PaymentNotFoundException(paymentReference));
            
        if (!payment.canBeRefunded()) {
            throw new PaymentProcessingException("Payment cannot be refunded in current state: " + payment.getStatus());
        }
        
        validateRefundAmount(payment, request.getAmount());
        
        try {
            // Process refund through gateway
            boolean refunded = paymentGatewayService.refundPayment(payment, request.getAmount());
            
            if (refunded) {
                updatePaymentStatus(payment, Payment.PaymentStatus.REFUNDED, 
                                  "Payment refunded: " + request.getReason());
                
                // Update account balance
                Account account = payment.getAccount();
                account.setBalance(account.getBalance().add(request.getAmount()));
                accountRepository.save(account);
                
                auditLog("PAYMENT_REFUNDED", "PAYMENT", payment.getPaymentReference(), 
                        "Payment refunded amount: " + request.getAmount() + ", reason: " + request.getReason());
                
                // Send notifications
                notificationService.sendPaymentRefundNotification(payment, request.getAmount());
                webhookService.sendPaymentWebhook(payment, "payment.refunded");
            } else {
                throw new PaymentProcessingException("Refund processing failed");
            }
            
        } catch (Exception e) {
            log.error("Payment refund failed for payment: {}", paymentReference, e);
            throw new PaymentProcessingException("Payment refund failed", e);
        }
        
        PaymentResponse response = mapToPaymentResponse(payment);
        logMethodExit("refundPayment", response);
        return response;
    }

    /**
     * Update payment status
     */
    @Transactional
    public PaymentResponse updatePaymentStatus(String paymentReference, UpdatePaymentStatusRequest request) {
        logMethodEntry("updatePaymentStatus", paymentReference, request);
        
        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
            .orElseThrow(() -> new PaymentNotFoundException(paymentReference));
            
        Payment.PaymentStatus newStatus = Payment.PaymentStatus.valueOf(request.getStatus());
        updatePaymentStatus(payment, newStatus, request.getReason());
        
        auditLog("PAYMENT_STATUS_UPDATED", "PAYMENT", payment.getPaymentReference(), 
                "Status updated to: " + newStatus + ", reason: " + request.getReason());
        
        PaymentResponse response = mapToPaymentResponse(payment);
        logMethodExit("updatePaymentStatus", response);
        return response;
    }

    /**
     * Get payment by reference
     */
    @Cacheable(value = "payments", key = "#paymentReference")
    public PaymentResponse getPaymentByReference(String paymentReference) {
        logMethodEntry("getPaymentByReference", paymentReference);
        
        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
            .orElseThrow(() -> new PaymentNotFoundException(paymentReference));
            
        PaymentResponse response = mapToPaymentResponse(payment);
        logMethodExit("getPaymentByReference", response);
        return response;
    }

    /**
     * Get payments for account
     */
    public Page<PaymentResponse> getPaymentsForAccount(String accountNumber, Pageable pageable) {
        logMethodEntry("getPaymentsForAccount", accountNumber, pageable);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException(accountNumber));
            
        Page<Payment> payments = paymentRepository.findByAccountOrderByCreatedAtDesc(account, pageable);
        Page<PaymentResponse> response = payments.map(this::mapToPaymentResponse);
        
        logMethodExit("getPaymentsForAccount", response.getTotalElements());
        return response;
    }

    /**
     * Get payments by status
     */
    public Page<PaymentResponse> getPaymentsByStatus(Payment.PaymentStatus status, Pageable pageable) {
        logMethodEntry("getPaymentsByStatus", status, pageable);
        
        Page<Payment> payments = paymentRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        Page<PaymentResponse> response = payments.map(this::mapToPaymentResponse);
        
        logMethodExit("getPaymentsByStatus", response.getTotalElements());
        return response;
    }

    /**
     * Cancel payment
     */
    @Transactional
    public PaymentResponse cancelPayment(String paymentReference, String reason) {
        logMethodEntry("cancelPayment", paymentReference, reason);
        
        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
            .orElseThrow(() -> new PaymentNotFoundException(paymentReference));
            
        if (!payment.canBeCancelled()) {
            throw new PaymentProcessingException("Payment cannot be cancelled in current state: " + payment.getStatus());
        }
        
        updatePaymentStatus(payment, Payment.PaymentStatus.CANCELLED, reason);
        
        auditLog("PAYMENT_CANCELLED", "PAYMENT", payment.getPaymentReference(), 
                "Payment cancelled, reason: " + reason);
        
        // Send notifications
        notificationService.sendPaymentCancelledNotification(payment);
        webhookService.sendPaymentWebhook(payment, "payment.cancelled");
        
        PaymentResponse response = mapToPaymentResponse(payment);
        logMethodExit("cancelPayment", response);
        return response;
    }

    // Private helper methods

    private void validateCreatePaymentRequest(CreatePaymentRequest request) {
        validateRequired(request.getAccountNumber(), "accountNumber");
        validateRequired(request.getAmount(), "amount");
        validateRequired(request.getCurrencyCode(), "currencyCode");
        
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be greater than zero");
        }
    }

    private void validateAccountStatus(Account account) {
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new ValidationException("Account is not active: " + account.getStatus());
        }
    }

    private Payment createPaymentEntity(CreatePaymentRequest request, Account account, PaymentMethod paymentMethod) {
        Payment payment = new Payment();
        payment.setPaymentReference(generatePaymentReference());
        payment.setAccount(account);
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(request.getAmount());
        payment.setCurrencyCode(request.getCurrencyCode());
        payment.setDescription(request.getDescription());
        payment.setMerchantReference(request.getMerchantReference());
        payment.setCallbackUrl(request.getCallbackUrl());
        payment.setSuccessUrl(request.getSuccessUrl());
        payment.setFailureUrl(request.getFailureUrl());
        payment.setMetadata(request.getMetadata());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        
        // Set expiration time (24 hours from now)
        payment.setExpiresAt(OffsetDateTime.now().plusHours(24));
        
        return payment;
    }

    private String generatePaymentReference() {
        return "PAY_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private void createStatusHistory(Payment payment, Payment.PaymentStatus status, String reason) {
        PaymentStatusHistory history = new PaymentStatusHistory();
        history.setPayment(payment);
        history.setStatus(status);
        history.setReason(reason);
        history.setChangedBy(getCurrentUsername());
        history.setChangedAt(OffsetDateTime.now());
        
        if (payment.getStatusHistory() == null) {
            payment.setStatusHistory(List.of(history));
        } else {
            payment.getStatusHistory().add(history);
        }
    }

    private void updatePaymentStatus(Payment payment, Payment.PaymentStatus newStatus, String reason) {
        Payment.PaymentStatus oldStatus = payment.getStatus();
        payment.setStatus(newStatus);
        payment.setUpdatedAt(OffsetDateTime.now());
        
        createStatusHistory(payment, newStatus, reason);
        paymentRepository.save(payment);
        
        log.info("Payment status updated: {} from {} to {}", 
                payment.getPaymentReference(), oldStatus, newStatus);
    }

    private void validateRefundAmount(Payment payment, BigDecimal refundAmount) {
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Refund amount must be greater than zero");
        }
        
        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new ValidationException("Refund amount cannot exceed original payment amount");
        }
    }

    private void handlePaymentProcessingFailure(Long paymentId, String errorMessage) {
        try {
            Payment payment = paymentRepository.findById(paymentId).orElse(null);
            if (payment != null) {
                updatePaymentStatus(payment, Payment.PaymentStatus.FAILED, 
                                  "Async processing failed: " + errorMessage);
            }
        } catch (Exception e) {
            log.error("Failed to handle payment processing failure for payment: {}", paymentId, e);
        }
    }

    public CompletableFuture<Void> processPaymentFallback(Long paymentId, Exception ex) {
        log.error("Circuit breaker activated for payment processing: {}", paymentId, ex);
        handlePaymentProcessingFailure(paymentId, "Circuit breaker activated");
        return CompletableFuture.completedFuture(null);
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setPaymentReference(payment.getPaymentReference());
        response.setAccountNumber(payment.getAccount().getAccountNumber());
        response.setAmount(payment.getAmount());
        response.setCurrencyCode(payment.getCurrencyCode());
        response.setDescription(payment.getDescription());
        response.setStatus(payment.getStatus().toString());
        response.setMerchantReference(payment.getMerchantReference());
        response.setRiskScore(payment.getRiskScore());
        response.setProcessedAt(payment.getProcessedAt());
        response.setExpiresAt(payment.getExpiresAt());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        response.setMetadata(payment.getMetadata());
        return response;
    }
}