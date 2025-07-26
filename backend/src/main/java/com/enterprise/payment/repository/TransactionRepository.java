package com.enterprise.payment.repository;

import com.enterprise.payment.entity.Payment;
import com.enterprise.payment.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionReference(String transactionReference);
    
    List<Transaction> findByPaymentId(Long paymentId);
    
    Page<Transaction> findByPaymentIdOrderByCreatedAtDesc(Long paymentId, Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE t.payment = :payment ORDER BY t.createdAt DESC")
    Page<Transaction> findByPaymentOrderByCreatedAtDesc(@Param("payment") com.enterprise.payment.entity.Payment payment, Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE t.payment.account = :account ORDER BY t.createdAt DESC")
    Page<Transaction> findByPayment_AccountOrderByCreatedAtDesc(@Param("account") com.enterprise.payment.entity.Account account, Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    Page<Transaction> findByCreatedAtBetweenOrderByCreatedAtDesc(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate, Pageable pageable);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.payment.account = :account")
    long countByPayment_Account(@Param("account") com.enterprise.payment.entity.Account account);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);
    
    List<Transaction> findByType(Transaction.TransactionType type);
    
    Page<Transaction> findByTypeOrderByCreatedAtDesc(Transaction.TransactionType type, Pageable pageable);
    
    List<Transaction> findByStatus(Payment.PaymentStatus status);
    
    Page<Transaction> findByStatusOrderByCreatedAtDesc(Payment.PaymentStatus status, Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE t.payment.id = :paymentId AND t.type = :type")
    List<Transaction> findByPaymentIdAndType(@Param("paymentId") Long paymentId, 
                                           @Param("type") Transaction.TransactionType type);
    
    @Query("SELECT t FROM Transaction t WHERE t.payment.id = :paymentId AND t.status = :status")
    List<Transaction> findByPaymentIdAndStatus(@Param("paymentId") Long paymentId, 
                                             @Param("status") Payment.PaymentStatus status);
    
    @Query("SELECT t FROM Transaction t WHERE t.type = :type AND t.status = :status")
    List<Transaction> findByTypeAndStatus(@Param("type") Transaction.TransactionType type, 
                                        @Param("status") Payment.PaymentStatus status);
    
    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    List<Transaction> findByDateRange(@Param("startDate") OffsetDateTime startDate, 
                                    @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.processedAt BETWEEN :startDate AND :endDate")
    List<Transaction> findByProcessedDateRange(@Param("startDate") OffsetDateTime startDate, 
                                             @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.type = :type AND t.createdAt BETWEEN :startDate AND :endDate")
    List<Transaction> findByTypeAndDateRange(@Param("type") Transaction.TransactionType type,
                                           @Param("startDate") OffsetDateTime startDate, 
                                           @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.status = :status AND t.createdAt BETWEEN :startDate AND :endDate")
    List<Transaction> findByStatusAndDateRange(@Param("status") Payment.PaymentStatus status,
                                             @Param("startDate") OffsetDateTime startDate, 
                                             @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.payment.account.id = :accountId ORDER BY t.createdAt DESC")
    Page<Transaction> findByAccountIdOrderByCreatedAtDesc(@Param("accountId") Long accountId, Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE t.payment.account.id = :accountId AND t.type = :type")
    List<Transaction> findByAccountIdAndType(@Param("accountId") Long accountId, 
                                           @Param("type") Transaction.TransactionType type);
    
    @Query("SELECT t FROM Transaction t WHERE t.payment.account.id = :accountId AND t.status = :status")
    List<Transaction> findByAccountIdAndStatus(@Param("accountId") Long accountId, 
                                             @Param("status") Payment.PaymentStatus status);
    
    @Query("SELECT t FROM Transaction t WHERE t.amount >= :minAmount AND t.amount <= :maxAmount")
    List<Transaction> findByAmountRange(@Param("minAmount") BigDecimal minAmount, 
                                      @Param("maxAmount") BigDecimal maxAmount);
    
    @Query("SELECT t FROM Transaction t WHERE t.gatewayTransactionId = :gatewayId")
    Optional<Transaction> findByGatewayTransactionId(@Param("gatewayId") String gatewayTransactionId);
    
    @Query("SELECT t FROM Transaction t WHERE t.status = 'PENDING' AND t.createdAt < :cutoffTime")
    List<Transaction> findStaleTransactions(@Param("cutoffTime") OffsetDateTime cutoffTime);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.type = :type")
    long countByType(@Param("type") Transaction.TransactionType type);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = :status")
    long countByStatus(@Param("status") Payment.PaymentStatus status);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.payment.account.id = :accountId")
    long countByAccountId(@Param("accountId") Long accountId);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.createdAt >= :since")
    long countTransactionsSince(@Param("since") OffsetDateTime since);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.status = 'COMPLETED' AND t.type = :type")
    BigDecimal getTotalAmountByTypeAndCompleted(@Param("type") Transaction.TransactionType type);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.status = 'COMPLETED' AND " +
           "t.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalCompletedAmountInDateRange(@Param("startDate") OffsetDateTime startDate, 
                                                @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT SUM(t.processingFee) FROM Transaction t WHERE t.status = 'COMPLETED' AND " +
           "t.processedAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalProcessingFeesInDateRange(@Param("startDate") OffsetDateTime startDate, 
                                               @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT t.type, COUNT(t) FROM Transaction t GROUP BY t.type")
    List<Object[]> getTransactionTypeStatistics();
    
    @Query("SELECT t.status, COUNT(t) FROM Transaction t GROUP BY t.status")
    List<Object[]> getTransactionStatusStatistics();
    
    @Query("SELECT DATE(t.createdAt) as date, COUNT(t) as count, SUM(t.amount) as total " +
           "FROM Transaction t WHERE t.createdAt >= :since GROUP BY DATE(t.createdAt) ORDER BY date")
    List<Object[]> getDailyTransactionStatistics(@Param("since") OffsetDateTime since);
    
    @Query("SELECT t.type, DATE(t.createdAt) as date, COUNT(t) as count, SUM(t.amount) as total " +
           "FROM Transaction t WHERE t.createdAt >= :since " +
           "GROUP BY t.type, DATE(t.createdAt) ORDER BY date, t.type")
    List<Object[]> getDailyTransactionStatisticsByType(@Param("since") OffsetDateTime since);
    
    @Query("SELECT AVG(t.amount) FROM Transaction t WHERE t.status = 'COMPLETED' AND t.type = :type")
    BigDecimal getAverageTransactionAmount(@Param("type") Transaction.TransactionType type);
    
    boolean existsByTransactionReference(String transactionReference);
    
    boolean existsByGatewayTransactionId(String gatewayTransactionId);
    
    // Additional methods for TransactionService
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.payment.account = :account AND t.type = :type")
    Long countByPayment_AccountAndType(@Param("account") com.enterprise.payment.entity.Account account, @Param("type") Transaction.TransactionType type);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.payment.account = :account")
    BigDecimal sumAmountByPayment_Account(@Param("account") com.enterprise.payment.entity.Account account);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.payment.account = :account AND t.createdAt > :createdAt")
    Long countByPayment_AccountAndCreatedAtAfter(@Param("account") com.enterprise.payment.entity.Account account, @Param("createdAt") OffsetDateTime createdAt);
    
    @Query("SELECT t FROM Transaction t WHERE t.payment.account = :account AND t.createdAt > :createdAt ORDER BY t.createdAt DESC")
    List<Transaction> findByPayment_AccountAndCreatedAtAfterOrderByCreatedAtDesc(@Param("account") com.enterprise.payment.entity.Account account, @Param("createdAt") OffsetDateTime createdAt);
}