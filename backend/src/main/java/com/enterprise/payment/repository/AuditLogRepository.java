package com.enterprise.payment.repository;

import com.enterprise.payment.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityType(String entityType);
    
    Page<AuditLog> findByEntityTypeOrderByCreatedAtDesc(String entityType, Pageable pageable);
    
    List<AuditLog> findByEntityId(Long entityId);
    
    Page<AuditLog> findByEntityIdOrderByCreatedAtDesc(Long entityId, Pageable pageable);
    
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);
    
    Page<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId, Pageable pageable);
    
    List<AuditLog> findByCorrelationId(String correlationId);
    
    Page<AuditLog> findByCorrelationIdOrderByCreatedAtDesc(String correlationId, Pageable pageable);
    
    List<AuditLog> findByUserId(Long userId);
    
    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    List<AuditLog> findByAction(String action);
    
    Page<AuditLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);
    
    @Query("SELECT al FROM AuditLog al WHERE al.entityType = :entityType AND al.entityId = :entityId AND al.action = :action")
    List<AuditLog> findByEntityTypeAndEntityIdAndAction(@Param("entityType") String entityType,
                                                       @Param("entityId") Long entityId,
                                                       @Param("action") String action);
    
    @Query("SELECT al FROM AuditLog al WHERE al.user.id = :userId AND al.action = :action")
    List<AuditLog> findByUserIdAndAction(@Param("userId") Long userId, @Param("action") String action);
    
    @Query("SELECT al FROM AuditLog al WHERE al.entityType = :entityType AND al.action = :action")
    List<AuditLog> findByEntityTypeAndAction(@Param("entityType") String entityType, @Param("action") String action);
    
    @Query("SELECT al FROM AuditLog al WHERE al.createdAt BETWEEN :startDate AND :endDate")
    List<AuditLog> findByDateRange(@Param("startDate") OffsetDateTime startDate, 
                                 @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT al FROM AuditLog al WHERE al.createdAt BETWEEN :startDate AND :endDate ORDER BY al.createdAt DESC")
    Page<AuditLog> findByDateRangeOrderByCreatedAtDesc(@Param("startDate") OffsetDateTime startDate, 
                                                      @Param("endDate") OffsetDateTime endDate, 
                                                      Pageable pageable);
    
    @Query("SELECT al FROM AuditLog al WHERE al.entityType = :entityType AND al.createdAt BETWEEN :startDate AND :endDate")
    List<AuditLog> findByEntityTypeAndDateRange(@Param("entityType") String entityType,
                                              @Param("startDate") OffsetDateTime startDate, 
                                              @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT al FROM AuditLog al WHERE al.user.id = :userId AND al.createdAt BETWEEN :startDate AND :endDate")
    List<AuditLog> findByUserIdAndDateRange(@Param("userId") Long userId,
                                          @Param("startDate") OffsetDateTime startDate, 
                                          @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT al FROM AuditLog al WHERE al.correlationId = :correlationId AND al.createdAt BETWEEN :startDate AND :endDate")
    List<AuditLog> findByCorrelationIdAndDateRange(@Param("correlationId") String correlationId,
                                                  @Param("startDate") OffsetDateTime startDate, 
                                                  @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT al FROM AuditLog al WHERE al.action = :action AND al.createdAt BETWEEN :startDate AND :endDate")
    List<AuditLog> findByActionAndDateRange(@Param("action") String action,
                                          @Param("startDate") OffsetDateTime startDate, 
                                          @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT al FROM AuditLog al WHERE al.ipAddress = :ipAddress")
    List<AuditLog> findByIpAddress(@Param("ipAddress") String ipAddress);
    
    @Query("SELECT al FROM AuditLog al WHERE al.ipAddress = :ipAddress AND al.createdAt BETWEEN :startDate AND :endDate")
    List<AuditLog> findByIpAddressAndDateRange(@Param("ipAddress") String ipAddress,
                                             @Param("startDate") OffsetDateTime startDate, 
                                             @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT al FROM AuditLog al WHERE al.user IS NULL")
    List<AuditLog> findSystemGeneratedLogs();
    
    @Query("SELECT al FROM AuditLog al WHERE al.user IS NOT NULL")
    List<AuditLog> findUserGeneratedLogs();
    
    @Query("SELECT al FROM AuditLog al WHERE al.entityType IN :entityTypes")
    List<AuditLog> findByEntityTypes(@Param("entityTypes") List<String> entityTypes);
    
    @Query("SELECT al FROM AuditLog al WHERE al.action IN :actions")
    List<AuditLog> findByActions(@Param("actions") List<String> actions);
    
    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.entityType = :entityType")
    long countByEntityType(@Param("entityType") String entityType);
    
    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.entityType = :entityType AND al.entityId = :entityId")
    long countByEntityTypeAndEntityId(@Param("entityType") String entityType, @Param("entityId") Long entityId);
    
    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.action = :action")
    long countByAction(@Param("action") String action);
    
    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.createdAt >= :since")
    long countLogsSince(@Param("since") OffsetDateTime since);
    
    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.createdAt BETWEEN :startDate AND :endDate")
    long countLogsBetween(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT al.entityType, COUNT(al) FROM AuditLog al GROUP BY al.entityType")
    List<Object[]> getAuditLogEntityTypeStatistics();
    
    @Query("SELECT al.action, COUNT(al) FROM AuditLog al GROUP BY al.action")
    List<Object[]> getAuditLogActionStatistics();
    
    @Query("SELECT DATE(al.createdAt) as date, COUNT(al) as count FROM AuditLog al " +
           "WHERE al.createdAt >= :since GROUP BY DATE(al.createdAt) ORDER BY date")
    List<Object[]> getDailyAuditLogStatistics(@Param("since") OffsetDateTime since);
    
    @Query("SELECT al.entityType, DATE(al.createdAt) as date, COUNT(al) as count FROM AuditLog al " +
           "WHERE al.createdAt >= :since GROUP BY al.entityType, DATE(al.createdAt) ORDER BY date, al.entityType")
    List<Object[]> getDailyAuditLogStatisticsByEntityType(@Param("since") OffsetDateTime since);
    
    @Query("SELECT al.action, DATE(al.createdAt) as date, COUNT(al) as count FROM AuditLog al " +
           "WHERE al.createdAt >= :since GROUP BY al.action, DATE(al.createdAt) ORDER BY date, al.action")
    List<Object[]> getDailyAuditLogStatisticsByAction(@Param("since") OffsetDateTime since);
    
    @Query("SELECT DISTINCT al.entityType FROM AuditLog al")
    List<String> findDistinctEntityTypes();
    
    @Query("SELECT DISTINCT al.action FROM AuditLog al")
    List<String> findDistinctActions();
    
    @Query("SELECT DISTINCT al.correlationId FROM AuditLog al WHERE al.correlationId IS NOT NULL")
    List<String> findDistinctCorrelationIds();
}