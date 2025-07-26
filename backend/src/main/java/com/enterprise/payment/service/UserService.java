package com.enterprise.payment.service;

import com.enterprise.payment.dto.request.CreateUserRequest;
import com.enterprise.payment.dto.response.UserResponse;
import com.enterprise.payment.entity.Account;
import com.enterprise.payment.entity.User;
import com.enterprise.payment.exception.AccountNotFoundException;
import com.enterprise.payment.exception.UserNotFoundException;
import com.enterprise.payment.exception.ValidationException;
import com.enterprise.payment.repository.AccountRepository;
import com.enterprise.payment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing user operations including authentication support
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService extends BaseService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Load user details for Spring Security authentication
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logMethodEntry("loadUserByUsername", username);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            
        logMethodExit("loadUserByUsername", user.getUsername());
        return user; // User entity implements UserDetails
    }

    /**
     * Create a new user
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        logMethodEntry("createUser", request);
        
        validateCreateUserRequest(request);
        validateUserUniqueness(request.getUsername(), request.getEmail());
        
        Account account = null;
        if (request.getAccountId() != null) {
            account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> AccountNotFoundException.byId(request.getAccountId()));
        }
        
        User user = createUserEntity(request, account);
        user = userRepository.save(user);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("role", user.getRole());
        metadata.put("accountId", account != null ? account.getId() : null);
        
        auditLog("USER_CREATED", "USER", user.getId(), 
                "User created with role: " + user.getRole(), metadata);
        
        UserResponse response = mapToUserResponse(user);
        logMethodExit("createUser", response);
        return response;
    }

    /**
     * Get user by username
     */
    @Cacheable(value = "users", key = "#username")
    public UserResponse getUserByUsername(String username) {
        logMethodEntry("getUserByUsername", username);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));
            
        UserResponse response = mapToUserResponse(user);
        logMethodExit("getUserByUsername", response);
        return response;
    }

    /**
     * Get user by ID
     */
    @Cacheable(value = "users", key = "#userId")
    public UserResponse getUserById(Long userId) {
        logMethodEntry("getUserById", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
            
        UserResponse response = mapToUserResponse(user);
        logMethodExit("getUserById", response);
        return response;
    }

    /**
     * Get user by email
     */
    public UserResponse getUserByEmail(String email) {
        logMethodEntry("getUserByEmail", email);
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("email: " + email));
            
        UserResponse response = mapToUserResponse(user);
        logMethodExit("getUserByEmail", response);
        return response;
    }

    /**
     * Get all users with pagination
     */
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        logMethodEntry("getAllUsers", pageable);
        
        Page<User> users = userRepository.findAll(pageable);
        Page<UserResponse> response = users.map(this::mapToUserResponse);
        
        logMethodExit("getAllUsers", response.getTotalElements());
        return response;
    }

    /**
     * Get users by role
     */
    public Page<UserResponse> getUsersByRole(User.UserRole role, Pageable pageable) {
        logMethodEntry("getUsersByRole", role, pageable);
        
        Page<User> users = userRepository.findByRoleOrderByCreatedAtDesc(role, pageable);
        Page<UserResponse> response = users.map(this::mapToUserResponse);
        
        logMethodExit("getUsersByRole", response.getTotalElements());
        return response;
    }

    /**
     * Get users by account
     */
    public Page<UserResponse> getUsersByAccount(String accountNumber, Pageable pageable) {
        logMethodEntry("getUsersByAccount", accountNumber, pageable);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> AccountNotFoundException.byAccountNumber(accountNumber));
            
        Page<User> users = userRepository.findByAccountIdOrderByCreatedAtDesc(account.getId(), pageable);
        Page<UserResponse> response = users.map(this::mapToUserResponse);
        
        logMethodExit("getUsersByAccount", response.getTotalElements());
        return response;
    }

    /**
     * Update user status
     */
    @Transactional
    @CacheEvict(value = "users", key = "#username")
    public UserResponse updateUserStatus(String username, boolean isActive) {
        logMethodEntry("updateUserStatus", username, isActive);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));
            
        Boolean oldStatus = user.getIsActive();
        user.setIsActive(isActive);
        user.setUpdatedAt(OffsetDateTime.now());
        
        // Clear failed login attempts when activating user
        if (isActive && !oldStatus) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        }
        
        user = userRepository.save(user);
        
        auditLog("USER_STATUS_UPDATED", "USER", user.getId(), 
                String.format("Status updated from %s to %s", oldStatus, isActive));
        
        log.info("User status updated: {} from {} to {}", username, oldStatus, isActive);
        
        UserResponse response = mapToUserResponse(user);
        logMethodExit("updateUserStatus", response);
        return response;
    }

    /**
     * Update user role
     */
    @Transactional
    @CacheEvict(value = "users", key = "#username")
    public UserResponse updateUserRole(String username, User.UserRole newRole) {
        logMethodEntry("updateUserRole", username, newRole);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));
            
        User.UserRole oldRole = user.getRole();
        user.setRole(newRole);
        user.setUpdatedAt(OffsetDateTime.now());
        
        user = userRepository.save(user);
        
        auditLog("USER_ROLE_UPDATED", "USER", user.getId(), 
                String.format("Role updated from %s to %s", oldRole, newRole));
        
        log.info("User role updated: {} from {} to {}", username, oldRole, newRole);
        
        UserResponse response = mapToUserResponse(user);
        logMethodExit("updateUserRole", response);
        return response;
    }

    /**
     * Update user password
     */
    @Transactional
    @CacheEvict(value = "users", key = "#username")
    public void updateUserPassword(String username, String newPassword) {
        logMethodEntry("updateUserPassword", username, "***");
        
        validateRequired(newPassword, "newPassword");
        validatePasswordStrength(newPassword);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));
            
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(encodedPassword);
        user.setUpdatedAt(OffsetDateTime.now());
        
        userRepository.save(user);
        
        auditLog("USER_PASSWORD_UPDATED", "USER", user.getId(), "Password updated");
        
        log.info("User password updated: {}", username);
        logMethodExit("updateUserPassword");
    }

    /**
     * Update user profile
     */
    @Transactional
    @CacheEvict(value = "users", key = "#username")
    public UserResponse updateUserProfile(String username, String firstName, String lastName, String email) {
        logMethodEntry("updateUserProfile", username, firstName, lastName, email);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));
            
        if (firstName != null && !firstName.trim().isEmpty()) {
            user.setFirstName(firstName.trim());
        }
        
        if (lastName != null && !lastName.trim().isEmpty()) {
            user.setLastName(lastName.trim());
        }
        
        if (email != null && !email.trim().isEmpty()) {
            String trimmedEmail = email.trim().toLowerCase();
            // Check if email is already taken by another user
            if (!user.getEmail().equals(trimmedEmail) && userRepository.existsByEmail(trimmedEmail)) {
                throw new ValidationException("Email is already taken by another user");
            }
            user.setEmail(trimmedEmail);
        }
        
        user.setUpdatedAt(OffsetDateTime.now());
        user = userRepository.save(user);
        
        auditLog("USER_PROFILE_UPDATED", "USER", user.getId(), "Profile information updated");
        
        UserResponse response = mapToUserResponse(user);
        logMethodExit("updateUserProfile", response);
        return response;
    }

    /**
     * Record login attempt
     */
    @Transactional
    @CacheEvict(value = "users", key = "#username")
    public void recordLoginAttempt(String username, boolean successful) {
        logMethodEntry("recordLoginAttempt", username, successful);
        
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            logMethodExit("recordLoginAttempt", "User not found");
            return;
        }
        
        if (successful) {
            user.setLastLogin(OffsetDateTime.now());
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            
            auditLog("USER_LOGIN_SUCCESS", "USER", user.getId(), "Successful login");
        } else {
            int failedAttempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(failedAttempts);
            
            // Lock account after 5 failed attempts for 30 minutes
            if (failedAttempts >= 5) {
                user.setLockedUntil(OffsetDateTime.now().plusMinutes(30));
                auditLog("USER_ACCOUNT_LOCKED", "USER", user.getId(), 
                        "Account locked due to failed login attempts: " + failedAttempts);
            } else {
                auditLog("USER_LOGIN_FAILED", "USER", user.getId(), 
                        "Failed login attempt: " + failedAttempts);
            }
        }
        
        userRepository.save(user);
        logMethodExit("recordLoginAttempt");
    }

    /**
     * Check if user account is locked
     */
    public boolean isAccountLocked(String username) {
        logMethodEntry("isAccountLocked", username);
        
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return false;
        }
        
        boolean locked = !user.isAccountNonLocked();
        logMethodExit("isAccountLocked", locked);
        return locked;
    }

    /**
     * Unlock user account
     */
    @Transactional
    @CacheEvict(value = "users", key = "#username")
    public UserResponse unlockAccount(String username) {
        logMethodEntry("unlockAccount", username);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));
            
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setUpdatedAt(OffsetDateTime.now());
        
        user = userRepository.save(user);
        
        auditLog("USER_ACCOUNT_UNLOCKED", "USER", user.getId(), "Account manually unlocked");
        
        log.info("User account unlocked: {}", username);
        
        UserResponse response = mapToUserResponse(user);
        logMethodExit("unlockAccount", response);
        return response;
    }

    /**
     * Delete user (soft delete by deactivating)
     */
    @Transactional
    @CacheEvict(value = "users", key = "#username")
    public void deleteUser(String username) {
        logMethodEntry("deleteUser", username);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));
            
        user.setIsActive(false);
        user.setUpdatedAt(OffsetDateTime.now());
        
        userRepository.save(user);
        
        auditLog("USER_DELETED", "USER", user.getId(), "User soft deleted");
        
        log.info("User soft deleted: {}", username);
        logMethodExit("deleteUser");
    }

    // Private helper methods

    private void validateCreateUserRequest(CreateUserRequest request) {
        validateRequired(request.getUsername(), "username");
        validateRequired(request.getEmail(), "email");
        validateRequired(request.getPassword(), "password");
        validateRequired(request.getFirstName(), "firstName");
        validateRequired(request.getLastName(), "lastName");
        
        validatePasswordStrength(request.getPassword());
        validateUsernameFormat(request.getUsername());
        validateEmailFormat(request.getEmail());
    }

    private void validateUserUniqueness(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new ValidationException("Username is already taken");
        }
        
        if (userRepository.existsByEmail(email)) {
            throw new ValidationException("Email is already registered");
        }
    }

    private void validatePasswordStrength(String password) {
        if (password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long");
        }
        
        if (!password.matches(".*[A-Z].*")) {
            throw new ValidationException("Password must contain at least one uppercase letter");
        }
        
        if (!password.matches(".*[a-z].*")) {
            throw new ValidationException("Password must contain at least one lowercase letter");
        }
        
        if (!password.matches(".*[0-9].*")) {
            throw new ValidationException("Password must contain at least one digit");
        }
        
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            throw new ValidationException("Password must contain at least one special character");
        }
    }

    private void validateUsernameFormat(String username) {
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new ValidationException("Username can only contain letters, numbers, and underscores");
        }
        
        if (username.length() < 3 || username.length() > 50) {
            throw new ValidationException("Username must be between 3 and 50 characters");
        }
    }

    private void validateEmailFormat(String email) {
        if (!email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
            throw new ValidationException("Invalid email format");
        }
    }

    private User createUserEntity(CreateUserRequest request, Account account) {
        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setRole(request.getRole() != null ? request.getRole() : User.UserRole.USER);
        user.setAccount(account);
        user.setIsActive(true);
        user.setFailedLoginAttempts(0);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        
        return user;
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setFullName(user.getFullName());
        response.setRole(user.getRole());
        response.setIsActive(user.getIsActive());
        response.setLastLogin(user.getLastLogin());
        response.setFailedLoginAttempts(user.getFailedLoginAttempts());
        response.setIsAccountLocked(!user.isAccountNonLocked());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        
        // Set account information if available
        if (user.getAccount() != null) {
            UserResponse.AccountSummary accountSummary = new UserResponse.AccountSummary();
            accountSummary.setId(user.getAccount().getId());
            accountSummary.setAccountNumber(user.getAccount().getAccountNumber());
            accountSummary.setAccountName(user.getAccount().getAccountName());
            accountSummary.setStatus(user.getAccount().getStatus());
            accountSummary.setCurrencyCode(user.getAccount().getCurrencyCode());
            response.setAccount(accountSummary);
        }
        
        return response;
    }
}