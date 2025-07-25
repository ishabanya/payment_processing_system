package com.enterprise.payment.service;

import com.enterprise.payment.dto.request.CreateAccountRequest;
import com.enterprise.payment.dto.response.AccountResponse;
import com.enterprise.payment.entity.Account;
import com.enterprise.payment.exception.AccountNotFoundException;
import com.enterprise.payment.exception.InsufficientFundsException;
import com.enterprise.payment.exception.ValidationException;
import com.enterprise.payment.repository.AccountRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing account operations including balance management and validation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService extends BaseService {

    private final AccountRepository accountRepository;

    /**
     * Create a new account
     */
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        logMethodEntry("createAccount", request);
        
        validateCreateAccountRequest(request);
        validateAccountUniqueness(request.getEmail());
        
        Account account = createAccountEntity(request);
        account = accountRepository.save(account);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("initialBalance", account.getBalance());
        
        auditLog("ACCOUNT_CREATED", "ACCOUNT", account.getAccountNumber(), 
                "Account created with initial balance: " + account.getBalance(), metadata);
        
        AccountResponse response = mapToAccountResponse(account);
        logMethodExit("createAccount", response);
        return response;
    }

    /**
     * Get account by account number
     */
    @Cacheable(value = "accounts", key = "#accountNumber")
    public AccountResponse getAccountByNumber(String accountNumber) {
        logMethodEntry("getAccountByNumber", accountNumber);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException(accountNumber));
            
        AccountResponse response = mapToAccountResponse(account);
        logMethodExit("getAccountByNumber", response);
        return response;
    }

    /**
     * Get account by ID
     */
    @Cacheable(value = "accounts", key = "#accountId")
    public AccountResponse getAccountById(Long accountId) {
        logMethodEntry("getAccountById", accountId);
        
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException(accountId));
            
        AccountResponse response = mapToAccountResponse(account);
        logMethodExit("getAccountById", response);
        return response;
    }

    /**
     * Get all accounts with pagination
     */
    public Page<AccountResponse> getAllAccounts(Pageable pageable) {
        logMethodEntry("getAllAccounts", pageable);
        
        Page<Account> accounts = accountRepository.findAll(pageable);
        Page<AccountResponse> response = accounts.map(this::mapToAccountResponse);
        
        logMethodExit("getAllAccounts", response.getTotalElements());
        return response;
    }

    /**
     * Get accounts by status
     */
    public Page<AccountResponse> getAccountsByStatus(Account.AccountStatus status, Pageable pageable) {
        logMethodEntry("getAccountsByStatus", status, pageable);
        
        Page<Account> accounts = accountRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        Page<AccountResponse> response = accounts.map(this::mapToAccountResponse);
        
        logMethodExit("getAccountsByStatus", response.getTotalElements());
        return response;
    }

    /**
     * Update account status
     */
    @Transactional
    @CacheEvict(value = "accounts", key = "#accountNumber")
    public AccountResponse updateAccountStatus(String accountNumber, Account.AccountStatus newStatus) {
        logMethodEntry("updateAccountStatus", accountNumber, newStatus);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException(accountNumber));
            
        Account.AccountStatus oldStatus = account.getStatus();
        account.setStatus(newStatus);
        account.setUpdatedBy(getCurrentUsername());
        account.setUpdatedAt(OffsetDateTime.now());
        
        account = accountRepository.save(account);
        
        auditLog("ACCOUNT_STATUS_UPDATED", "ACCOUNT", account.getAccountNumber(), 
                String.format("Status updated from %s to %s", oldStatus, newStatus));
        
        log.info("Account status updated: {} from {} to {}", accountNumber, oldStatus, newStatus);
        
        AccountResponse response = mapToAccountResponse(account);
        logMethodExit("updateAccountStatus", response);
        return response;
    }

    /**
     * Credit account balance
     */
    @Transactional
    @CacheEvict(value = "accounts", key = "#accountNumber")
    @CircuitBreaker(name = "balance-operations", fallbackMethod = "balanceOperationFallback")
    public AccountResponse creditBalance(String accountNumber, BigDecimal amount, String description) {
        logMethodEntry("creditBalance", accountNumber, amount, description);
        
        validateAmount(amount, "Credit amount");
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException(accountNumber));
            
        validateAccountForBalanceOperation(account);
        
        BigDecimal oldBalance = account.getBalance();
        BigDecimal newBalance = oldBalance.add(amount);
        
        account.setBalance(newBalance);
        account.setUpdatedBy(getCurrentUsername());
        account.setUpdatedAt(OffsetDateTime.now());
        
        account = accountRepository.save(account);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("oldBalance", oldBalance);
        metadata.put("newBalance", newBalance);
        metadata.put("creditAmount", amount);
        metadata.put("description", description);
        
        auditLog("BALANCE_CREDITED", "ACCOUNT", account.getAccountNumber(), 
                String.format("Balance credited: %s. Old: %s, New: %s", amount, oldBalance, newBalance), 
                metadata);
        
        log.info("Account balance credited: {} amount: {} new balance: {}", 
                accountNumber, amount, newBalance);
        
        AccountResponse response = mapToAccountResponse(account);
        logMethodExit("creditBalance", response);
        return response;
    }

    /**
     * Debit account balance
     */
    @Transactional
    @CacheEvict(value = "accounts", key = "#accountNumber")
    @CircuitBreaker(name = "balance-operations", fallbackMethod = "balanceOperationFallback")
    public AccountResponse debitBalance(String accountNumber, BigDecimal amount, String description) {
        logMethodEntry("debitBalance", accountNumber, amount, description);
        
        validateAmount(amount, "Debit amount");
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException(accountNumber));
            
        validateAccountForBalanceOperation(account);
        
        BigDecimal oldBalance = account.getBalance();
        
        if (oldBalance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(amount, oldBalance);
        }
        
        BigDecimal newBalance = oldBalance.subtract(amount);
        
        account.setBalance(newBalance);
        account.setUpdatedBy(getCurrentUsername());
        account.setUpdatedAt(OffsetDateTime.now());
        
        account = accountRepository.save(account);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("oldBalance", oldBalance);
        metadata.put("newBalance", newBalance);
        metadata.put("debitAmount", amount);
        metadata.put("description", description);
        
        auditLog("BALANCE_DEBITED", "ACCOUNT", account.getAccountNumber(), 
                String.format("Balance debited: %s. Old: %s, New: %s", amount, oldBalance, newBalance), 
                metadata);
        
        log.info("Account balance debited: {} amount: {} new balance: {}", 
                accountNumber, amount, newBalance);
        
        AccountResponse response = mapToAccountResponse(account);
        logMethodExit("debitBalance", response);
        return response;
    }

    /**
     * Transfer funds between accounts
     */
    @Transactional
    public Map<String, AccountResponse> transferFunds(String fromAccountNumber, String toAccountNumber, 
                                                     BigDecimal amount, String description) {
        logMethodEntry("transferFunds", fromAccountNumber, toAccountNumber, amount, description);
        
        validateAmount(amount, "Transfer amount");
        
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new ValidationException("Cannot transfer to the same account");
        }
        
        // Debit from source account
        AccountResponse fromAccount = debitBalance(fromAccountNumber, amount, 
                                                 "Transfer to " + toAccountNumber + ": " + description);
        
        // Credit to destination account
        AccountResponse toAccount = creditBalance(toAccountNumber, amount, 
                                                "Transfer from " + fromAccountNumber + ": " + description);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("fromAccount", fromAccountNumber);
        metadata.put("toAccount", toAccountNumber);
        metadata.put("amount", amount);
        metadata.put("description", description);
        
        auditLog("FUNDS_TRANSFERRED", "TRANSFER", fromAccountNumber + "->" + toAccountNumber, 
                String.format("Transferred %s from %s to %s", amount, fromAccountNumber, toAccountNumber), 
                metadata);
        
        Map<String, AccountResponse> result = new HashMap<>();
        result.put("fromAccount", fromAccount);
        result.put("toAccount", toAccount);
        
        logMethodExit("transferFunds", result);
        return result;
    }

    /**
     * Get account balance
     */
    @Cacheable(value = "account-balances", key = "#accountNumber")
    public BigDecimal getAccountBalance(String accountNumber) {
        logMethodEntry("getAccountBalance", accountNumber);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException(accountNumber));
            
        BigDecimal balance = account.getBalance();
        logMethodExit("getAccountBalance", balance);
        return balance;
    }

    /**
     * Validate account balance for payment
     */
    public boolean validateBalanceForPayment(String accountNumber, BigDecimal amount) {
        logMethodEntry("validateBalanceForPayment", accountNumber, amount);
        
        BigDecimal balance = getAccountBalance(accountNumber);
        boolean hasBalance = balance.compareTo(amount) >= 0;
        
        logMethodExit("validateBalanceForPayment", hasBalance);
        return hasBalance;
    }

    /**
     * Update account information
     */
    @Transactional
    @CacheEvict(value = "accounts", key = "#accountNumber")
    public AccountResponse updateAccount(String accountNumber, String accountName, String phone) {
        logMethodEntry("updateAccount", accountNumber, accountName, phone);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException(accountNumber));
            
        if (accountName != null && !accountName.trim().isEmpty()) {
            account.setAccountName(accountName.trim());
        }
        
        if (phone != null) {
            account.setPhone(phone.trim());
        }
        
        account.setUpdatedBy(getCurrentUsername());
        account.setUpdatedAt(OffsetDateTime.now());
        
        account = accountRepository.save(account);
        
        auditLog("ACCOUNT_UPDATED", "ACCOUNT", account.getAccountNumber(), 
                "Account information updated");
        
        AccountResponse response = mapToAccountResponse(account);
        logMethodExit("updateAccount", response);
        return response;
    }

    // Private helper methods

    private void validateCreateAccountRequest(CreateAccountRequest request) {
        validateRequired(request.getAccountName(), "accountName");
        validateRequired(request.getEmail(), "email");
        
        if (request.getInitialBalance() != null && request.getInitialBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Initial balance cannot be negative");
        }
    }

    private void validateAccountUniqueness(String email) {
        if (accountRepository.existsByEmail(email)) {
            throw new ValidationException("Account with this email already exists");
        }
    }

    private Account createAccountEntity(CreateAccountRequest request) {
        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setAccountName(request.getAccountName().trim());
        account.setEmail(request.getEmail().trim().toLowerCase());
        account.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        account.setStatus(Account.AccountStatus.ACTIVE);
        account.setBalance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO);
        account.setCurrencyCode(request.getCurrencyCode() != null ? request.getCurrencyCode() : "USD");
        account.setCreatedBy(getCurrentUsername());
        account.setUpdatedBy(getCurrentUsername());
        account.setCreatedAt(OffsetDateTime.now());
        account.setUpdatedAt(OffsetDateTime.now());
        
        return account;
    }

    private String generateAccountNumber() {
        return "ACC_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private void validateAmount(BigDecimal amount, String fieldName) {
        validateRequired(amount, fieldName);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(fieldName + " must be greater than zero");
        }
    }

    private void validateAccountForBalanceOperation(Account account) {
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new ValidationException("Account is not active for balance operations: " + account.getStatus());
        }
    }

    public AccountResponse balanceOperationFallback(String accountNumber, BigDecimal amount, String description, Exception ex) {
        log.error("Circuit breaker activated for balance operation on account: {}", accountNumber, ex);
        throw new ValidationException("Balance operation temporarily unavailable. Please try again later.");
    }

    private AccountResponse mapToAccountResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountName(account.getAccountName());
        response.setEmail(account.getEmail());
        response.setPhone(account.getPhone());
        response.setStatus(account.getStatus().toString());
        response.setBalance(account.getBalance());
        response.setCurrencyCode(account.getCurrencyCode());
        response.setCreatedAt(account.getCreatedAt());
        response.setUpdatedAt(account.getUpdatedAt());
        return response;
    }
}