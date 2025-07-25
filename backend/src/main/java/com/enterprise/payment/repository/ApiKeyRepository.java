package com.enterprise.payment.repository;

import com.enterprise.payment.entity.ApiKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByKeyId(String keyId);
    
    List<ApiKey> findByAccountId(Long accountId);
    
    Page<ApiKey> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);
    
    List<ApiKey> findByIsActive(Boolean isActive);
    
    Page<ApiKey> findByIsActiveOrderByCreatedAtDesc(Boolean isActive, Pageable pageable);
    
    @Query("SELECT ak FROM ApiKey ak WHERE ak.keyId = :keyId AND ak.isActive = true")
    Optional<ApiKey> findActiveByKeyId(@Param("keyId") String keyId);
    
    @Query("SELECT ak FROM ApiKey ak WHERE ak.account.id = :accountId AND ak.isActive = true")
    List<ApiKey> findActiveByAccountId(@Param("accountId") Long accountId);
    
    @Query("SELECT ak FROM ApiKey ak WHERE ak.account.id = :accountId AND ak.isActive = true ORDER BY ak.createdAt DESC")
    Page<ApiKey> findActiveByAccountIdOrderByCreatedAtDesc(@Param("accountId") Long accountId, Pageable pageable);
    
    @Query("SELECT ak FROM ApiKey ak WHERE ak.account.id = :accountId AND ak.isActive = :isActive")
    List<ApiKey> findByAccountIdAndIsActive(@Param("accountId") Long accountId, @Param("isActive") Boolean isActive);
    
    @Query("SELECT ak FROM ApiKey ak WHERE ak.name LIKE %:name%")
    List<ApiKey> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT ak FROM ApiKey ak WHERE ak.account.id = :accountId AND ak.name LIKE %:name%")
    List<ApiKey> findByAccountIdAndNameContaining(@Param("accountId") Long accountId, @Param("name") String name);
    
    @Query("SELECT ak FROM ApiKey ak WHERE :permission MEMBER OF ak.permissions")
    List<ApiKey> findByPermission(@Param("permission") String permission);
    
    @Query("SELECT ak FROM ApiKey ak WHERE ak.account.id = :accountId AND :permission MEMBER OF ak.permissions")
    List<ApiKey> findByAccountIdAndPermission(@Param("accountId") Long accountId, @Param("permission") String permission);
    
    @Query("SELECT ak FROM ApiKey ak WHERE ak.account.id = :accountId AND :permission MEMBER OF ak.permissions AND ak.isActive = true")
    List<ApiKey> findActiveByAccountIdAndPermission(@Param("accountId") Long accountId, @Param("permission") String permission);
    
    @Query("SELECT ak FROM ApiKey ak WHERE ak.expiresAt IS NOT NULL AND ak.expiresAt < :now")
    List<ApiKey> findExpiredApiKeys(@Param("now") OffsetDateTime now);
    
    @Query("SELECT ak FROM ApiKey ak WHERE ak.expiresAt IS NOT NULL AND ak.expiresAt < :now AND ak.isActive = true")
    List<ApiKey> findExpiredActiveApiKeys(@Param("now") OffsetDateTime now);
    
    @Query("SELECT ak FROM ApiKey ak WHERE ak.account.id = :accountId AND ak.expiresAt IS NOT NULL AND ak.expiresAt < :now")
    List<ApiKey> findExpiredByAccountId(@Param("accountId") Long accountId, @Param("now") OffsetDateTime now);
    
    @Query("SELECT ak FROM ApiKey ak WHERE ak.expiresAt IS NOT NULL AND ak.expiresAt BETWEEN :startDate AND :endDate")
    List<ApiKey> findExpiringBetween(@Param("startDate") OffsetDateTime startDate, 
                                   @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT ak FROM ApiKey ak WHERE ak.lastUsedAt IS NULL")
    List<ApiKey> findNeverUsedApiKeys();
    
    @Query("SELECT ak FROM ApiKey ak WHERE ak.lastUsedAt < :cutoffDate")
    List<ApiKey> findUnusedSince(@Param("cutoffDate") OffsetDateTime cutoffDate);
    
    @Query("SELECT ak FROM ApiKey ak WHERE ak.account.id = :accountId AND ak.lastUsedAt IS NULL")
    List<ApiKey> findNeverUsedByAccountId(@Param("accountId") Long accountId);
    
    @Query("SELECT ak FROM ApiKey ak WHERE ak.account.id = :accountId AND ak.lastUsedAt < :cutoffDate")
    List<ApiKey> findUnusedByAccountIdSince(@Param("accountId") Long accountId, @Param("cutoffDate") OffsetDateTime cutoffDate);
    
    @Query("SELECT ak FROM ApiKey ak WHERE ak.createdAt BETWEEN :startDate AND :endDate")
    List<ApiKey> findCreatedBetween(@Param("startDate") OffsetDateTime startDate, 
                                  @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT ak FROM ApiKey ak WHERE ak.lastUsedAt BETWEEN :startDate AND :endDate")
    List<ApiKey> findUsedBetween(@Param("startDate") OffsetDateTime startDate, 
                               @Param("endDate") OffsetDateTime endDate);
    
    @Modifying
    @Query("UPDATE ApiKey ak SET ak.lastUsedAt = :now WHERE ak.keyId = :keyId")
    int updateLastUsedTime(@Param("keyId") String keyId, @Param("now") OffsetDateTime now);
    
    @Modifying
    @Query("UPDATE ApiKey ak SET ak.isActive = false WHERE ak.keyId = :keyId")
    int deactivateByKeyId(@Param("keyId") String keyId);
    
    @Modifying
    @Query("UPDATE ApiKey ak SET ak.isActive = false WHERE ak.account.id = :accountId")
    int deactivateAllByAccountId(@Param("accountId") Long accountId);
    
    @Modifying
    @Query("UPDATE ApiKey ak SET ak.isActive = false WHERE ak.expiresAt < :now AND ak.isActive = true")
    int deactivateExpiredApiKeys(@Param("now") OffsetDateTime now);
    
    @Query("SELECT COUNT(ak) FROM ApiKey ak WHERE ak.account.id = :accountId")
    long countByAccountId(@Param("accountId") Long accountId);
    
    @Query("SELECT COUNT(ak) FROM ApiKey ak WHERE ak.account.id = :accountId AND ak.isActive = true")
    long countActiveByAccountId(@Param("accountId") Long accountId);
    
    @Query("SELECT COUNT(ak) FROM ApiKey ak WHERE ak.isActive = :isActive")
    long countByIsActive(@Param("isActive") Boolean isActive);
    
    @Query("SELECT COUNT(ak) FROM ApiKey ak WHERE ak.expiresAt IS NOT NULL AND ak.expiresAt < :now")
    long countExpiredApiKeys(@Param("now") OffsetDateTime now);
    
    @Query("SELECT COUNT(ak) FROM ApiKey ak WHERE ak.lastUsedAt IS NULL")
    long countNeverUsed();
    
    @Query("SELECT COUNT(ak) FROM ApiKey ak WHERE ak.lastUsedAt < :cutoffDate")
    long countUnusedSince(@Param("cutoffDate") OffsetDateTime cutoffDate);
    
    @Query("SELECT COUNT(ak) FROM ApiKey ak WHERE ak.createdAt >= :since")
    long countCreatedSince(@Param("since") OffsetDateTime since);
    
    @Query("SELECT DATE(ak.createdAt) as date, COUNT(ak) as count FROM ApiKey ak " +
           "WHERE ak.createdAt >= :since GROUP BY DATE(ak.createdAt) ORDER BY date")
    List<Object[]> getDailyApiKeyCreationStatistics(@Param("since") OffsetDateTime since);
    
    @Query("SELECT DATE(ak.lastUsedAt) as date, COUNT(DISTINCT ak.id) as uniqueKeys, COUNT(ak.id) as totalUsage " +
           "FROM ApiKey ak WHERE ak.lastUsedAt >= :since " +
           "GROUP BY DATE(ak.lastUsedAt) ORDER BY date")
    List<Object[]> getDailyApiKeyUsageStatistics(@Param("since") OffsetDateTime since);
    
    @Query("SELECT COUNT(ak) as total, " +
           "SUM(CASE WHEN ak.isActive = true THEN 1 ELSE 0 END) as active, " +
           "SUM(CASE WHEN ak.expiresAt IS NOT NULL AND ak.expiresAt < :now THEN 1 ELSE 0 END) as expired, " +
           "SUM(CASE WHEN ak.lastUsedAt IS NULL THEN 1 ELSE 0 END) as neverUsed " +
           "FROM ApiKey ak")
    Object[] getApiKeyStatistics(@Param("now") OffsetDateTime now);
    
    @Query("SELECT permission FROM ApiKey ak JOIN ak.permissions permission GROUP BY permission ORDER BY COUNT(permission) DESC")
    List<String> getMostUsedPermissions();
    
    @Query("SELECT ak.account.id, COUNT(ak) as keyCount FROM ApiKey ak GROUP BY ak.account.id ORDER BY keyCount DESC")
    List<Object[]> getApiKeyCountByAccount();
    
    boolean existsByKeyId(String keyId);
    
    boolean existsByAccountIdAndName(Long accountId, String name);
}