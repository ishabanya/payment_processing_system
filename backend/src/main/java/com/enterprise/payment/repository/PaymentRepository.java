package com.enterprise.payment.repository;

import com.enterprise.payment.entity.Payment;
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
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentReference(String paymentReference);
    
    Page<Payment> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);
    
    Page<Payment> findByStatusOrderByCreatedAtDesc(Payment.PaymentStatus status, Pageable pageable);
    
    List<Payment> findByAccountIdAndStatus(Long accountId, Payment.PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.merchantReference = :merchantReference AND p.account.id = :accountId")
    Optional<Payment> findByMerchantReferenceAndAccountId(@Param("merchantReference") String merchantReference, 
                                                          @Param("accountId") Long accountId);
    
    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findByStatusAndDateRange(@Param("status") Payment.PaymentStatus status,
                                          @Param("startDate") OffsetDateTime startDate,
                                          @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT p FROM Payment p WHERE p.expiresAt < :now AND p.status = 'PENDING'")
    List<Payment> findExpiredPendingPayments(@Param("now") OffsetDateTime now);
    
    @Query("SELECT p FROM Payment p WHERE p.account.id = :accountId AND p.amount >= :minAmount AND p.amount <= :maxAmount")
    Page<Payment> findByAccountIdAndAmountRange(@Param("accountId") Long accountId,
                                               @Param("minAmount") BigDecimal minAmount,
                                               @Param("maxAmount") BigDecimal maxAmount,
                                               Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    long countByStatus(@Param("status") Payment.PaymentStatus status);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.processedAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalCompletedAmountInDateRange(@Param("startDate") OffsetDateTime startDate,
                                                  @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.account.id = :accountId AND p.createdAt >= :since")
    long countByAccountIdSince(@Param("accountId") Long accountId, @Param("since") OffsetDateTime since);
    
    @Query("SELECT p FROM Payment p WHERE p.description LIKE %:keyword% OR p.merchantReference LIKE %:keyword%")
    Page<Payment> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT p.status, COUNT(p) FROM Payment p GROUP BY p.status")
    List<Object[]> getPaymentStatusStatistics();
    
    @Query("SELECT DATE(p.createdAt) as date, COUNT(p) as count, SUM(p.amount) as total " +
           "FROM Payment p WHERE p.createdAt >= :since GROUP BY DATE(p.createdAt) ORDER BY date")
    List<Object[]> getDailyPaymentStatistics(@Param("since") OffsetDateTime since);
}