package com.enterprise.payment.repository;

import com.enterprise.payment.entity.WebhookDelivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, Long> {

    List<WebhookDelivery> findByWebhookId(Long webhookId);
    
    Page<WebhookDelivery> findByWebhookIdOrderByCreatedAtDesc(Long webhookId, Pageable pageable);
    
    List<WebhookDelivery> findByPaymentId(Long paymentId);
    
    Page<WebhookDelivery> findByPaymentIdOrderByCreatedAtDesc(Long paymentId, Pageable pageable);
    
    List<WebhookDelivery> findByEvent(String event);
    
    Page<WebhookDelivery> findByEventOrderByCreatedAtDesc(String event, Pageable pageable);
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.webhook.id = :webhookId AND wd.event = :event")
    List<WebhookDelivery> findByWebhookIdAndEvent(@Param("webhookId") Long webhookId, @Param("event") String event);
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.webhook.id = :webhookId AND wd.payment.id = :paymentId")
    List<WebhookDelivery> findByWebhookIdAndPaymentId(@Param("webhookId") Long webhookId, @Param("paymentId") Long paymentId);
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.deliveredAt IS NULL")
    List<WebhookDelivery> findUndelivered();
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.deliveredAt IS NULL ORDER BY wd.createdAt ASC")
    Page<WebhookDelivery> findUndeliveredOrderByCreatedAtAsc(Pageable pageable);
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.deliveredAt IS NOT NULL")
    List<WebhookDelivery> findDelivered();
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.deliveredAt IS NOT NULL ORDER BY wd.deliveredAt DESC")
    Page<WebhookDelivery> findDeliveredOrderByDeliveredAtDesc(Pageable pageable);
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.deliveredAt IS NULL AND wd.attempts < wd.webhook.retryAttempts")
    List<WebhookDelivery> findPendingRetries();
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.deliveredAt IS NULL AND wd.attempts < wd.webhook.retryAttempts " +
           "AND (wd.nextRetryAt IS NULL OR wd.nextRetryAt <= :now) ORDER BY wd.nextRetryAt ASC")
    List<WebhookDelivery> findReadyForRetry(@Param("now") OffsetDateTime now);
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.deliveredAt IS NULL AND wd.attempts >= wd.webhook.retryAttempts")
    List<WebhookDelivery> findFailedDeliveries();
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.deliveredAt IS NULL AND wd.attempts >= wd.webhook.retryAttempts " +
           "ORDER BY wd.createdAt DESC")
    Page<WebhookDelivery> findFailedDeliveriesOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.webhook.id = :webhookId AND wd.deliveredAt IS NULL AND wd.attempts >= wd.webhook.retryAttempts")
    List<WebhookDelivery> findFailedDeliveriesByWebhookId(@Param("webhookId") Long webhookId);
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.webhook.account.id = :accountId AND wd.deliveredAt IS NULL AND wd.attempts >= wd.webhook.retryAttempts")
    List<WebhookDelivery> findFailedDeliveriesByAccountId(@Param("accountId") Long accountId);
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.responseStatus IS NOT NULL AND wd.responseStatus >= :minStatus AND wd.responseStatus < :maxStatus")
    List<WebhookDelivery> findByResponseStatusRange(@Param("minStatus") Integer minStatus, @Param("maxStatus") Integer maxStatus);
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.responseStatus IS NOT NULL AND (wd.responseStatus < 200 OR wd.responseStatus >= 300)")
    List<WebhookDelivery> findWithErrorResponse();
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.responseStatus IS NOT NULL AND wd.responseStatus >= 200 AND wd.responseStatus < 300")
    List<WebhookDelivery> findWithSuccessResponse();
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.createdAt BETWEEN :startDate AND :endDate")
    List<WebhookDelivery> findCreatedBetween(@Param("startDate") OffsetDateTime startDate, 
                                           @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.deliveredAt BETWEEN :startDate AND :endDate")
    List<WebhookDelivery> findDeliveredBetween(@Param("startDate") OffsetDateTime startDate, 
                                             @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.webhook.id = :webhookId AND wd.createdAt BETWEEN :startDate AND :endDate")
    List<WebhookDelivery> findByWebhookIdAndCreatedBetween(@Param("webhookId") Long webhookId,
                                                          @Param("startDate") OffsetDateTime startDate, 
                                                          @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.webhook.account.id = :accountId AND wd.createdAt BETWEEN :startDate AND :endDate")
    List<WebhookDelivery> findByAccountIdAndCreatedBetween(@Param("accountId") Long accountId,
                                                          @Param("startDate") OffsetDateTime startDate, 
                                                          @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.attempts = :attempts")
    List<WebhookDelivery> findByAttempts(@Param("attempts") Integer attempts);
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.attempts > :minAttempts")
    List<WebhookDelivery> findByAttemptsGreaterThan(@Param("minAttempts") Integer minAttempts);
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.webhook.account.id = :accountId ORDER BY wd.createdAt DESC")
    Page<WebhookDelivery> findByAccountIdOrderByCreatedAtDesc(@Param("accountId") Long accountId, Pageable pageable);
    
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.nextRetryAt IS NOT NULL AND wd.nextRetryAt <= :now AND wd.deliveredAt IS NULL")
    List<WebhookDelivery> findDueForRetry(@Param("now") OffsetDateTime now);
    
    @Modifying
    @Query("UPDATE WebhookDelivery wd SET wd.deliveredAt = :now, wd.responseStatus = :status, wd.responseBody = :responseBody WHERE wd.id = :id")
    int markAsDelivered(@Param("id") Long id, @Param("now") OffsetDateTime now, 
                       @Param("status") Integer status, @Param("responseBody") String responseBody);
    
    @Modifying
    @Query("UPDATE WebhookDelivery wd SET wd.attempts = wd.attempts + 1, wd.nextRetryAt = :nextRetryAt WHERE wd.id = :id")
    int incrementAttempts(@Param("id") Long id, @Param("nextRetryAt") OffsetDateTime nextRetryAt);
    
    @Modifying
    @Query("DELETE FROM WebhookDelivery wd WHERE wd.createdAt < :cutoffDate AND wd.deliveredAt IS NOT NULL")
    int deleteOldDeliveredRecords(@Param("cutoffDate") OffsetDateTime cutoffDate);
    
    @Modifying
    @Query("DELETE FROM WebhookDelivery wd WHERE wd.createdAt < :cutoffDate AND wd.deliveredAt IS NULL AND wd.attempts >= wd.webhook.retryAttempts")
    int deleteOldFailedRecords(@Param("cutoffDate") OffsetDateTime cutoffDate);
    
    @Query("SELECT COUNT(wd) FROM WebhookDelivery wd WHERE wd.webhook.id = :webhookId")
    long countByWebhookId(@Param("webhookId") Long webhookId);
    
    @Query("SELECT COUNT(wd) FROM WebhookDelivery wd WHERE wd.webhook.account.id = :accountId")
    long countByAccountId(@Param("accountId") Long accountId);
    
    @Query("SELECT COUNT(wd) FROM WebhookDelivery wd WHERE wd.event = :event")
    long countByEvent(@Param("event") String event);
    
    @Query("SELECT COUNT(wd) FROM WebhookDelivery wd WHERE wd.deliveredAt IS NULL")
    long countUndelivered();
    
    @Query("SELECT COUNT(wd) FROM WebhookDelivery wd WHERE wd.deliveredAt IS NOT NULL")
    long countDelivered();
    
    @Query("SELECT COUNT(wd) FROM WebhookDelivery wd WHERE wd.deliveredAt IS NULL AND wd.attempts >= wd.webhook.retryAttempts")
    long countFailed();
    
    @Query("SELECT COUNT(wd) FROM WebhookDelivery wd WHERE wd.createdAt >= :since")
    long countCreatedSince(@Param("since") OffsetDateTime since);
    
    @Query("SELECT COUNT(wd) FROM WebhookDelivery wd WHERE wd.deliveredAt >= :since")
    long countDeliveredSince(@Param("since") OffsetDateTime since);
    
    @Query("SELECT wd.event, COUNT(wd) FROM WebhookDelivery wd GROUP BY wd.event ORDER BY COUNT(wd) DESC")
    List<Object[]> getEventStatistics();
    
    @Query("SELECT wd.responseStatus, COUNT(wd) FROM WebhookDelivery wd WHERE wd.responseStatus IS NOT NULL GROUP BY wd.responseStatus ORDER BY wd.responseStatus")
    List<Object[]> getResponseStatusStatistics();
    
    @Query("SELECT wd.attempts, COUNT(wd) FROM WebhookDelivery wd GROUP BY wd.attempts ORDER BY wd.attempts")
    List<Object[]> getAttemptsDistribution();
    
    @Query("SELECT DATE(wd.createdAt) as date, COUNT(wd) as total, " +
           "SUM(CASE WHEN wd.deliveredAt IS NOT NULL THEN 1 ELSE 0 END) as delivered, " +
           "SUM(CASE WHEN wd.deliveredAt IS NULL AND wd.attempts >= wd.webhook.retryAttempts THEN 1 ELSE 0 END) as failed " +
           "FROM WebhookDelivery wd WHERE wd.createdAt >= :since " +
           "GROUP BY DATE(wd.createdAt) ORDER BY date")
    List<Object[]> getDailyDeliveryStatistics(@Param("since") OffsetDateTime since);
    
    @Query("SELECT COUNT(wd) as total, " +
           "SUM(CASE WHEN wd.deliveredAt IS NOT NULL THEN 1 ELSE 0 END) as delivered, " +
           "SUM(CASE WHEN wd.deliveredAt IS NULL AND wd.attempts >= wd.webhook.retryAttempts THEN 1 ELSE 0 END) as failed, " +
           "SUM(CASE WHEN wd.deliveredAt IS NULL AND wd.attempts < wd.webhook.retryAttempts THEN 1 ELSE 0 END) as pending, " +
           "AVG(wd.attempts) as avgAttempts " +
           "FROM WebhookDelivery wd")
    Object[] getDeliveryStatistics();
    
    @Query("SELECT AVG(EXTRACT(EPOCH FROM (wd.deliveredAt - wd.createdAt))) FROM WebhookDelivery wd WHERE wd.deliveredAt IS NOT NULL")
    Double getAverageDeliveryTimeInSeconds();
}