package com.enterprise.payment.repository;

import com.enterprise.payment.entity.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    List<PaymentMethod> findByAccountId(Long accountId);
    
    Page<PaymentMethod> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);
    
    List<PaymentMethod> findByType(PaymentMethod.PaymentMethodType type);
    
    Page<PaymentMethod> findByTypeOrderByCreatedAtDesc(PaymentMethod.PaymentMethodType type, Pageable pageable);
    
    List<PaymentMethod> findByIsActive(Boolean isActive);
    
    Page<PaymentMethod> findByIsActiveOrderByCreatedAtDesc(Boolean isActive, Pageable pageable);
    
    List<PaymentMethod> findByIsDefault(Boolean isDefault);
    
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.account.id = :accountId AND pm.isActive = true")
    List<PaymentMethod> findActiveByAccountId(@Param("accountId") Long accountId);
    
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.account.id = :accountId AND pm.isActive = true " +
           "ORDER BY pm.isDefault DESC, pm.createdAt DESC")
    Page<PaymentMethod> findActiveByAccountIdOrderByDefaultFirst(@Param("accountId") Long accountId, Pageable pageable);
    
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.account.id = :accountId AND pm.type = :type")
    List<PaymentMethod> findByAccountIdAndType(@Param("accountId") Long accountId, 
                                             @Param("type") PaymentMethod.PaymentMethodType type);
    
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.account.id = :accountId AND pm.type = :type AND pm.isActive = true")
    List<PaymentMethod> findActiveByAccountIdAndType(@Param("accountId") Long accountId, 
                                                   @Param("type") PaymentMethod.PaymentMethodType type);
    
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.account.id = :accountId AND pm.isDefault = true")
    Optional<PaymentMethod> findDefaultByAccountId(@Param("accountId") Long accountId);
    
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.account.id = :accountId AND pm.isDefault = true AND pm.isActive = true")
    Optional<PaymentMethod> findActiveDefaultByAccountId(@Param("accountId") Long accountId);
    
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.account.id = :accountId AND pm.isActive = :isActive")
    List<PaymentMethod> findByAccountIdAndIsActive(@Param("accountId") Long accountId, 
                                                 @Param("isActive") Boolean isActive);
    
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.provider = :provider")
    List<PaymentMethod> findByProvider(@Param("provider") String provider);
    
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.provider = :provider AND pm.isActive = true")
    List<PaymentMethod> findActiveByProvider(@Param("provider") String provider);
    
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.lastFourDigits = :lastFour AND pm.account.id = :accountId")
    List<PaymentMethod> findByAccountIdAndLastFourDigits(@Param("accountId") Long accountId, 
                                                       @Param("lastFour") String lastFourDigits);
    
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.expiryYear < :year OR " +
           "(pm.expiryYear = :year AND pm.expiryMonth < :month)")
    List<PaymentMethod> findExpiredPaymentMethods(@Param("year") Integer year, @Param("month") Integer month);
    
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.expiryYear = :year AND pm.expiryMonth = :month AND pm.isActive = true")
    List<PaymentMethod> findExpiringThisMonth(@Param("year") Integer year, @Param("month") Integer month);
    
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.expiryYear = :year AND pm.expiryMonth BETWEEN :startMonth AND :endMonth AND pm.isActive = true")
    List<PaymentMethod> findExpiringInRange(@Param("year") Integer year, 
                                          @Param("startMonth") Integer startMonth, 
                                          @Param("endMonth") Integer endMonth);
    
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.createdAt BETWEEN :startDate AND :endDate")
    List<PaymentMethod> findCreatedBetween(@Param("startDate") OffsetDateTime startDate, 
                                         @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT COUNT(pm) FROM PaymentMethod pm WHERE pm.account.id = :accountId")
    long countByAccountId(@Param("accountId") Long accountId);
    
    @Query("SELECT COUNT(pm) FROM PaymentMethod pm WHERE pm.account.id = :accountId AND pm.isActive = true")
    long countActiveByAccountId(@Param("accountId") Long accountId);
    
    @Query("SELECT COUNT(pm) FROM PaymentMethod pm WHERE pm.type = :type")
    long countByType(@Param("type") PaymentMethod.PaymentMethodType type);
    
    @Query("SELECT COUNT(pm) FROM PaymentMethod pm WHERE pm.type = :type AND pm.isActive = true")
    long countActiveByType(@Param("type") PaymentMethod.PaymentMethodType type);
    
    @Query("SELECT COUNT(pm) FROM PaymentMethod pm WHERE pm.isActive = :isActive")
    long countByIsActive(@Param("isActive") Boolean isActive);
    
    @Query("SELECT pm.type, COUNT(pm) FROM PaymentMethod pm GROUP BY pm.type")
    List<Object[]> getPaymentMethodTypeStatistics();
    
    @Query("SELECT pm.type, COUNT(pm) FROM PaymentMethod pm WHERE pm.isActive = true GROUP BY pm.type")
    List<Object[]> getActivePaymentMethodTypeStatistics();
    
    @Query("SELECT pm.provider, COUNT(pm) FROM PaymentMethod pm GROUP BY pm.provider")
    List<Object[]> getPaymentMethodProviderStatistics();
    
    @Query("SELECT DATE(pm.createdAt) as date, COUNT(pm) as count FROM PaymentMethod pm " +
           "WHERE pm.createdAt >= :since GROUP BY DATE(pm.createdAt) ORDER BY date")
    List<Object[]> getDailyPaymentMethodCreationStatistics(@Param("since") OffsetDateTime since);
    
    @Query("SELECT pm.type, DATE(pm.createdAt) as date, COUNT(pm) as count FROM PaymentMethod pm " +
           "WHERE pm.createdAt >= :since GROUP BY pm.type, DATE(pm.createdAt) ORDER BY date, pm.type")
    List<Object[]> getDailyPaymentMethodCreationStatisticsByType(@Param("since") OffsetDateTime since);
    
    boolean existsByAccountIdAndIsDefaultAndIsActive(Long accountId, Boolean isDefault, Boolean isActive);
}