package com.enterprise.payment.repository;

import com.enterprise.payment.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);
    
    Optional<Account> findByEmail(String email);
    
    List<Account> findByStatus(Account.AccountStatus status);
    
    Page<Account> findByStatusOrderByCreatedAtDesc(Account.AccountStatus status, Pageable pageable);
    
    @Query("SELECT a FROM Account a WHERE a.balance >= :minBalance")
    List<Account> findAccountsWithMinimumBalance(@Param("minBalance") BigDecimal minBalance);
    
    @Query("SELECT a FROM Account a WHERE a.accountName LIKE %:name% OR a.email LIKE %:email%")
    Page<Account> searchByNameOrEmail(@Param("name") String name, @Param("email") String email, Pageable pageable);
    
    @Query("SELECT COUNT(a) FROM Account a WHERE a.status = :status")
    long countByStatus(@Param("status") Account.AccountStatus status);
    
    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.status = 'ACTIVE'")
    BigDecimal getTotalActiveBalance();
    
    boolean existsByAccountNumber(String accountNumber);
    
    boolean existsByEmail(String email);
}