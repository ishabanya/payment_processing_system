package com.enterprise.payment.repository;

import com.enterprise.payment.entity.RefreshToken;
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
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);
    
    List<RefreshToken> findByUserId(Long userId);
    
    Page<RefreshToken> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    List<RefreshToken> findByIsRevoked(Boolean isRevoked);
    
    Page<RefreshToken> findByIsRevokedOrderByCreatedAtDesc(Boolean isRevoked, Pageable pageable);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.isRevoked = false")
    List<RefreshToken> findActiveByUserId(@Param("userId") Long userId);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.isRevoked = false ORDER BY rt.createdAt DESC")
    Page<RefreshToken> findActiveByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token AND rt.isRevoked = false")
    Optional<RefreshToken> findActiveByToken(@Param("token") String token);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiresAt < :now")
    List<RefreshToken> findExpiredTokens(@Param("now") OffsetDateTime now);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiresAt < :now AND rt.isRevoked = false")
    List<RefreshToken> findExpiredActiveTokens(@Param("now") OffsetDateTime now);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.expiresAt < :now")
    List<RefreshToken> findExpiredTokensByUserId(@Param("userId") Long userId, @Param("now") OffsetDateTime now);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.isRevoked = true")
    List<RefreshToken> findRevokedByUserId(@Param("userId") Long userId);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.createdAt BETWEEN :startDate AND :endDate")
    List<RefreshToken> findCreatedBetween(@Param("startDate") OffsetDateTime startDate, 
                                        @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.createdAt BETWEEN :startDate AND :endDate")
    List<RefreshToken> findByUserIdAndCreatedBetween(@Param("userId") Long userId,
                                                   @Param("startDate") OffsetDateTime startDate, 
                                                   @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiresAt BETWEEN :startDate AND :endDate")
    List<RefreshToken> findExpiringBetween(@Param("startDate") OffsetDateTime startDate, 
                                         @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiresAt < :expiredBefore AND rt.isRevoked = false")
    List<RefreshToken> findTokensToCleanup(@Param("expiredBefore") OffsetDateTime expiredBefore);
    
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.user.id = :userId AND rt.isRevoked = false")
    int revokeAllByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.token = :token")
    int revokeByToken(@Param("token") String token);
    
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.expiresAt < :now AND rt.isRevoked = false")
    int revokeExpiredTokens(@Param("now") OffsetDateTime now);
    
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :cutoffDate")
    int deleteExpiredTokens(@Param("cutoffDate") OffsetDateTime cutoffDate);
    
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.isRevoked = true AND rt.createdAt < :cutoffDate")
    int deleteRevokedTokens(@Param("cutoffDate") OffsetDateTime cutoffDate);
    
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.isRevoked = false")
    long countActiveByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.isRevoked = :isRevoked")
    long countByIsRevoked(@Param("isRevoked") Boolean isRevoked);
    
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.expiresAt < :now")
    long countExpiredTokens(@Param("now") OffsetDateTime now);
    
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.expiresAt < :now AND rt.isRevoked = false")
    long countExpiredActiveTokens(@Param("now") OffsetDateTime now);
    
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.createdAt >= :since")
    long countTokensCreatedSince(@Param("since") OffsetDateTime since);
    
    @Query("SELECT DATE(rt.createdAt) as date, COUNT(rt) as count FROM RefreshToken rt " +
           "WHERE rt.createdAt >= :since GROUP BY DATE(rt.createdAt) ORDER BY date")
    List<Object[]> getDailyTokenCreationStatistics(@Param("since") OffsetDateTime since);
    
    @Query("SELECT COUNT(rt) as total, " +
           "SUM(CASE WHEN rt.isRevoked = false THEN 1 ELSE 0 END) as active, " +
           "SUM(CASE WHEN rt.expiresAt < :now THEN 1 ELSE 0 END) as expired " +
           "FROM RefreshToken rt")
    Object[] getTokenStatistics(@Param("now") OffsetDateTime now);
    
    boolean existsByToken(String token);
    
    boolean existsByUserIdAndIsRevoked(Long userId, Boolean isRevoked);
    
    // Additional methods needed by AuthenticationService
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    void deleteByUser(@Param("user") com.enterprise.payment.entity.User user);
    
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user AND rt.expiresAt < :expiresAt")
    void deleteByUserAndExpiresAtBefore(@Param("user") com.enterprise.payment.entity.User user, 
                                      @Param("expiresAt") OffsetDateTime expiresAt);
}