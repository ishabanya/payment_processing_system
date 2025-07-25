package com.enterprise.payment.repository;

import com.enterprise.payment.entity.Webhook;
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
public interface WebhookRepository extends JpaRepository<Webhook, Long> {

    List<Webhook> findByAccountId(Long accountId);
    
    Page<Webhook> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);
    
    List<Webhook> findByIsActive(Boolean isActive);
    
    Page<Webhook> findByIsActiveOrderByCreatedAtDesc(Boolean isActive, Pageable pageable);
    
    @Query("SELECT w FROM Webhook w WHERE w.account.id = :accountId AND w.isActive = true")
    List<Webhook> findActiveByAccountId(@Param("accountId") Long accountId);
    
    @Query("SELECT w FROM Webhook w WHERE w.account.id = :accountId AND w.isActive = true ORDER BY w.createdAt DESC")
    Page<Webhook> findActiveByAccountIdOrderByCreatedAtDesc(@Param("accountId") Long accountId, Pageable pageable);
    
    @Query("SELECT w FROM Webhook w WHERE w.account.id = :accountId AND w.isActive = :isActive")
    List<Webhook> findByAccountIdAndIsActive(@Param("accountId") Long accountId, @Param("isActive") Boolean isActive);
    
    @Query("SELECT w FROM Webhook w WHERE w.url = :url")
    List<Webhook> findByUrl(@Param("url") String url);
    
    @Query("SELECT w FROM Webhook w WHERE w.account.id = :accountId AND w.url = :url")
    List<Webhook> findByAccountIdAndUrl(@Param("accountId") Long accountId, @Param("url") String url);
    
    @Query("SELECT w FROM Webhook w WHERE w.account.id = :accountId AND w.url = :url AND w.isActive = true")
    List<Webhook> findActiveByAccountIdAndUrl(@Param("accountId") Long accountId, @Param("url") String url);
    
    @Query("SELECT w FROM Webhook w WHERE :event MEMBER OF w.events")
    List<Webhook> findByEvent(@Param("event") String event);
    
    @Query("SELECT w FROM Webhook w WHERE :event MEMBER OF w.events AND w.isActive = true")
    List<Webhook> findActiveByEvent(@Param("event") String event);
    
    @Query("SELECT w FROM Webhook w WHERE w.account.id = :accountId AND :event MEMBER OF w.events")
    List<Webhook> findByAccountIdAndEvent(@Param("accountId") Long accountId, @Param("event") String event);
    
    @Query("SELECT w FROM Webhook w WHERE w.account.id = :accountId AND :event MEMBER OF w.events AND w.isActive = true")
    List<Webhook> findActiveByAccountIdAndEvent(@Param("accountId") Long accountId, @Param("event") String event);
    
    @Query("SELECT w FROM Webhook w WHERE w.url LIKE %:domain%")
    List<Webhook> findByUrlContaining(@Param("domain") String domain);
    
    @Query("SELECT w FROM Webhook w WHERE w.account.id = :accountId AND w.url LIKE %:domain%")
    List<Webhook> findByAccountIdAndUrlContaining(@Param("accountId") Long accountId, @Param("domain") String domain);
    
    @Query("SELECT w FROM Webhook w WHERE w.retryAttempts = :retryAttempts")
    List<Webhook> findByRetryAttempts(@Param("retryAttempts") Integer retryAttempts);
    
    @Query("SELECT w FROM Webhook w WHERE w.retryAttempts > :minRetries")
    List<Webhook> findByRetryAttemptsGreaterThan(@Param("minRetries") Integer minRetries);
    
    @Query("SELECT w FROM Webhook w WHERE w.createdAt BETWEEN :startDate AND :endDate")
    List<Webhook> findCreatedBetween(@Param("startDate") OffsetDateTime startDate, 
                                   @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT w FROM Webhook w WHERE w.account.id = :accountId AND w.createdAt BETWEEN :startDate AND :endDate")
    List<Webhook> findByAccountIdAndCreatedBetween(@Param("accountId") Long accountId,
                                                  @Param("startDate") OffsetDateTime startDate, 
                                                  @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT w FROM Webhook w WHERE w.updatedAt BETWEEN :startDate AND :endDate")
    List<Webhook> findUpdatedBetween(@Param("startDate") OffsetDateTime startDate, 
                                   @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT DISTINCT w FROM Webhook w JOIN w.events e WHERE e IN :events AND w.isActive = true")
    List<Webhook> findActiveByAnyEvent(@Param("events") List<String> events);
    
    @Query("SELECT DISTINCT w FROM Webhook w JOIN w.events e WHERE w.account.id = :accountId AND e IN :events AND w.isActive = true")
    List<Webhook> findActiveByAccountIdAndAnyEvent(@Param("accountId") Long accountId, @Param("events") List<String> events);
    
    @Modifying
    @Query("UPDATE Webhook w SET w.isActive = false WHERE w.id = :id")
    int deactivateById(@Param("id") Long id);
    
    @Modifying
    @Query("UPDATE Webhook w SET w.isActive = false WHERE w.account.id = :accountId")
    int deactivateAllByAccountId(@Param("accountId") Long accountId);
    
    @Modifying
    @Query("UPDATE Webhook w SET w.retryAttempts = :retryAttempts WHERE w.id = :id")
    int updateRetryAttempts(@Param("id") Long id, @Param("retryAttempts") Integer retryAttempts);
    
    @Query("SELECT COUNT(w) FROM Webhook w WHERE w.account.id = :accountId")
    long countByAccountId(@Param("accountId") Long accountId);
    
    @Query("SELECT COUNT(w) FROM Webhook w WHERE w.account.id = :accountId AND w.isActive = true")
    long countActiveByAccountId(@Param("accountId") Long accountId);
    
    @Query("SELECT COUNT(w) FROM Webhook w WHERE w.isActive = :isActive")
    long countByIsActive(@Param("isActive") Boolean isActive);
    
    @Query("SELECT COUNT(w) FROM Webhook w WHERE :event MEMBER OF w.events")
    long countByEvent(@Param("event") String event);
    
    @Query("SELECT COUNT(w) FROM Webhook w WHERE :event MEMBER OF w.events AND w.isActive = true")
    long countActiveByEvent(@Param("event") String event);
    
    @Query("SELECT COUNT(w) FROM Webhook w WHERE w.createdAt >= :since")
    long countCreatedSince(@Param("since") OffsetDateTime since);
    
    @Query("SELECT DATE(w.createdAt) as date, COUNT(w) as count FROM Webhook w " +
           "WHERE w.createdAt >= :since GROUP BY DATE(w.createdAt) ORDER BY date")
    List<Object[]> getDailyWebhookCreationStatistics(@Param("since") OffsetDateTime since);
    
    @Query("SELECT COUNT(w) as total, " +
           "SUM(CASE WHEN w.isActive = true THEN 1 ELSE 0 END) as active, " +
           "AVG(w.retryAttempts) as avgRetryAttempts " +
           "FROM Webhook w")
    Object[] getWebhookStatistics();
    
    @Query("SELECT w.account.id, COUNT(w) as webhookCount FROM Webhook w GROUP BY w.account.id ORDER BY webhookCount DESC")
    List<Object[]> getWebhookCountByAccount();
    
    @Query("SELECT event FROM Webhook w JOIN w.events event GROUP BY event ORDER BY COUNT(event) DESC")
    List<String> getMostSubscribedEvents();
    
    @Query("SELECT w.retryAttempts, COUNT(w) FROM Webhook w GROUP BY w.retryAttempts ORDER BY w.retryAttempts")
    List<Object[]> getRetryAttemptsDistribution();
    
    boolean existsByAccountIdAndUrl(Long accountId, String url);
    
    boolean existsByAccountIdAndUrlAndIsActive(Long accountId, String url, Boolean isActive);
}