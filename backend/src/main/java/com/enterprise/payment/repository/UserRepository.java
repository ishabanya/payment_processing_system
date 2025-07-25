package com.enterprise.payment.repository;

import com.enterprise.payment.entity.User;
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
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    List<User> findByAccountId(Long accountId);
    
    List<User> findByRole(User.UserRole role);
    
    Page<User> findByRoleOrderByCreatedAtDesc(User.UserRole role, Pageable pageable);
    
    List<User> findByIsActive(Boolean isActive);
    
    Page<User> findByIsActiveOrderByCreatedAtDesc(Boolean isActive, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.isActive = true")
    Optional<User> findActiveByUsername(@Param("username") String username);
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findActiveByEmail(@Param("email") String email);
    
    @Query("SELECT u FROM User u WHERE u.account.id = :accountId AND u.isActive = true")
    List<User> findActiveUsersByAccountId(@Param("accountId") Long accountId);
    
    @Query("SELECT u FROM User u WHERE u.account.id = :accountId AND u.role = :role")
    List<User> findByAccountIdAndRole(@Param("accountId") Long accountId, @Param("role") User.UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.lockedUntil IS NOT NULL AND u.lockedUntil > :now")
    List<User> findLockedUsers(@Param("now") OffsetDateTime now);
    
    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts >= :maxAttempts AND u.lockedUntil IS NULL")
    List<User> findUsersWithExcessiveFailedAttempts(@Param("maxAttempts") Integer maxAttempts);
    
    @Query("SELECT u FROM User u WHERE u.lastLogin < :cutoffDate OR u.lastLogin IS NULL")
    List<User> findInactiveUsersSince(@Param("cutoffDate") OffsetDateTime cutoffDate);
    
    @Query("SELECT u FROM User u WHERE u.firstName LIKE %:name% OR u.lastName LIKE %:name% OR u.username LIKE %:name%")
    Page<User> searchByName(@Param("name") String name, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.account.id = :accountId AND " +
           "(u.firstName LIKE %:keyword% OR u.lastName LIKE %:keyword% OR u.username LIKE %:keyword% OR u.email LIKE %:keyword%)")
    Page<User> searchByAccountAndKeyword(@Param("accountId") Long accountId, @Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") User.UserRole role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = :isActive")
    long countByIsActive(@Param("isActive") Boolean isActive);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.account.id = :accountId")
    long countByAccountId(@Param("accountId") Long accountId);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLogin >= :since")
    long countActiveUsersSince(@Param("since") OffsetDateTime since);
    
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> getUserRoleStatistics();
    
    @Query("SELECT DATE(u.createdAt) as date, COUNT(u) as count FROM User u " +
           "WHERE u.createdAt >= :since GROUP BY DATE(u.createdAt) ORDER BY date")
    List<Object[]> getDailyUserRegistrationStatistics(@Param("since") OffsetDateTime since);
    
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersCreatedBetween(@Param("startDate") OffsetDateTime startDate, 
                                      @Param("endDate") OffsetDateTime endDate);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByUsernameAndAccountId(String username, Long accountId);
}