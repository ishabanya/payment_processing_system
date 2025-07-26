package com.enterprise.payment.service;

import com.enterprise.payment.dto.response.TransactionResponse;
import com.enterprise.payment.entity.Account;
import com.enterprise.payment.entity.Payment;
import com.enterprise.payment.entity.Transaction;
import com.enterprise.payment.exception.AccountNotFoundException;
import com.enterprise.payment.exception.PaymentNotFoundException;
import com.enterprise.payment.repository.AccountRepository;
import com.enterprise.payment.repository.PaymentRepository;
import com.enterprise.payment.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing transaction processing and analytics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService extends BaseService {

    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;

    /**
     * Create a new transaction
     */
    @Transactional
    public TransactionResponse createTransaction(String paymentReference, Transaction.TransactionType type,
                                               BigDecimal amount, String description) {
        logMethodEntry("createTransaction", paymentReference, type, amount, description);
        
        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
            .orElseThrow(() -> new PaymentNotFoundException(paymentReference));
            
        Transaction transaction = createTransactionEntity(payment, type, amount, description);
        transaction = transactionRepository.save(transaction);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("type", type);
        metadata.put("amount", amount);
        metadata.put("paymentReference", paymentReference);
        
        auditLog("TRANSACTION_CREATED", "TRANSACTION", transaction.getId(), 
                "Transaction created for payment: " + paymentReference, metadata);
        
        TransactionResponse response = mapToTransactionResponse(transaction);
        logMethodExit("createTransaction", response);
        return response;
    }

    /**
     * Get transaction by ID
     */
    @Cacheable(value = "transactions", key = "#transactionId")
    public TransactionResponse getTransactionById(Long transactionId) {
        logMethodEntry("getTransactionById", transactionId);
        
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + transactionId));
            
        TransactionResponse response = mapToTransactionResponse(transaction);
        logMethodExit("getTransactionById", response);
        return response;
    }

    /**
     * Get transactions for payment
     */
    public Page<TransactionResponse> getTransactionsForPayment(String paymentReference, Pageable pageable) {
        logMethodEntry("getTransactionsForPayment", paymentReference, pageable);
        
        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
            .orElseThrow(() -> new PaymentNotFoundException(paymentReference));
            
        Page<Transaction> transactions = transactionRepository.findByPaymentOrderByCreatedAtDesc(payment, pageable);
        Page<TransactionResponse> response = transactions.map(this::mapToTransactionResponse);
        
        logMethodExit("getTransactionsForPayment", response.getTotalElements());
        return response;
    }

    /**
     * Get transactions for account
     */
    public Page<TransactionResponse> getTransactionsForAccount(String accountNumber, Pageable pageable) {
        logMethodEntry("getTransactionsForAccount", accountNumber, pageable);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException(accountNumber));
            
        Page<Transaction> transactions = transactionRepository.findByPayment_AccountOrderByCreatedAtDesc(account, pageable);
        Page<TransactionResponse> response = transactions.map(this::mapToTransactionResponse);
        
        logMethodExit("getTransactionsForAccount", response.getTotalElements());
        return response;
    }

    /**
     * Get transactions by type
     */
    public Page<TransactionResponse> getTransactionsByType(Transaction.TransactionType type, Pageable pageable) {
        logMethodEntry("getTransactionsByType", type, pageable);
        
        Page<Transaction> transactions = transactionRepository.findByTypeOrderByCreatedAtDesc(type, pageable);
        Page<TransactionResponse> response = transactions.map(this::mapToTransactionResponse);
        
        logMethodExit("getTransactionsByType", response.getTotalElements());
        return response;
    }

    /**
     * Get transactions by date range
     */
    public Page<TransactionResponse> getTransactionsByDateRange(OffsetDateTime startDate, OffsetDateTime endDate, 
                                                              Pageable pageable) {
        logMethodEntry("getTransactionsByDateRange", startDate, endDate, pageable);
        
        Page<Transaction> transactions = transactionRepository
            .findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate, pageable);
        Page<TransactionResponse> response = transactions.map(this::mapToTransactionResponse);
        
        logMethodExit("getTransactionsByDateRange", response.getTotalElements());
        return response;
    }

    /**
     * Get transaction statistics for account
     */
    @Cacheable(value = "transaction-stats", key = "#accountNumber")
    public Map<String, Object> getTransactionStatistics(String accountNumber) {
        logMethodEntry("getTransactionStatistics", accountNumber);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException(accountNumber));
            
        Map<String, Object> stats = new HashMap<>();
        
        // Get total transaction count
        Long totalCount = transactionRepository.countByPayment_Account(account);
        stats.put("totalTransactions", totalCount);
        
        // Get transaction count by type
        for (Transaction.TransactionType type : Transaction.TransactionType.values()) {
            Long count = transactionRepository.countByPayment_AccountAndType(account, type);
            stats.put(type.name().toLowerCase() + "Transactions", count);
        }
        
        // Get total transaction amounts
        BigDecimal totalAmount = transactionRepository.sumAmountByPayment_Account(account);
        stats.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);
        
        // Get recent transactions count (last 30 days)
        OffsetDateTime thirtyDaysAgo = OffsetDateTime.now().minusDays(30);
        Long recentCount = transactionRepository
            .countByPayment_AccountAndCreatedAtAfter(account, thirtyDaysAgo);
        stats.put("recentTransactions", recentCount);
        
        logMethodExit("getTransactionStatistics", stats);
        return stats;
    }

    /**
     * Get daily transaction summary
     */
    public List<Map<String, Object>> getDailyTransactionSummary(String accountNumber, int days) {
        logMethodEntry("getDailyTransactionSummary", accountNumber, days);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException(accountNumber));
            
        OffsetDateTime startDate = OffsetDateTime.now().minusDays(days);
        
        // This would typically be implemented with a native query for better performance
        List<Transaction> transactions = transactionRepository
            .findByPayment_AccountAndCreatedAtAfterOrderByCreatedAtDesc(account, startDate);
        
        // Group transactions by date
        Map<String, Map<String, Object>> dailySummary = new HashMap<>();
        
        for (Transaction transaction : transactions) {
            String date = transaction.getCreatedAt().toLocalDate().toString();
            
            dailySummary.computeIfAbsent(date, k -> {
                Map<String, Object> dayStats = new HashMap<>();
                dayStats.put("date", date);
                dayStats.put("count", 0L);
                dayStats.put("totalAmount", BigDecimal.ZERO);
                return dayStats;
            });
            
            Map<String, Object> dayStats = dailySummary.get(date);
            dayStats.put("count", (Long) dayStats.get("count") + 1);
            dayStats.put("totalAmount", 
                        ((BigDecimal) dayStats.get("totalAmount")).add(transaction.getAmount()));
        }
        
        List<Map<String, Object>> result = List.copyOf(dailySummary.values());
        logMethodExit("getDailyTransactionSummary", result.size());
        return result;
    }

    /**
     * Process refund transaction
     */
    @Transactional
    public TransactionResponse processRefund(String paymentReference, BigDecimal refundAmount, String reason) {
        logMethodEntry("processRefund", paymentReference, refundAmount, reason);
        
        TransactionResponse response = createTransaction(paymentReference, 
                                                       Transaction.TransactionType.REFUND, 
                                                       refundAmount, 
                                                       "Refund: " + reason);
        
        logMethodExit("processRefund", response);
        return response;
    }

    // Private helper methods

    private Transaction createTransactionEntity(Payment payment, Transaction.TransactionType type, 
                                              BigDecimal amount, String description) {
        Transaction transaction = new Transaction();
        transaction.setTransactionReference(generateTransactionReference());
        transaction.setPayment(payment);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setCurrencyCode(payment.getCurrencyCode());
        transaction.setDescription(description);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setProcessedAt(OffsetDateTime.now());
        transaction.setCreatedAt(OffsetDateTime.now());
        transaction.setUpdatedAt(OffsetDateTime.now());
        
        return transaction;
    }

    private String generateTransactionReference() {
        return "TXN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setTransactionReference(transaction.getTransactionReference());
        response.setPaymentReference(transaction.getPayment().getPaymentReference());
        response.setType(transaction.getType().toString());
        response.setAmount(transaction.getAmount());
        response.setCurrencyCode(transaction.getCurrencyCode());
        response.setDescription(transaction.getDescription());
        response.setStatus(transaction.getStatus().toString());
        response.setProcessedAt(transaction.getProcessedAt());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setUpdatedAt(transaction.getUpdatedAt());
        return response;
    }
}