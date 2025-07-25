package com.enterprise.payment.service;

import com.enterprise.payment.dto.request.LoginRequest;
import com.enterprise.payment.dto.request.RegisterRequest;
import com.enterprise.payment.dto.response.AuthResponse;
import com.enterprise.payment.dto.response.UserResponse;
import com.enterprise.payment.entity.RefreshToken;
import com.enterprise.payment.entity.User;
import com.enterprise.payment.exception.AuthenticationException;
import com.enterprise.payment.exception.UserNotFoundException;
import com.enterprise.payment.exception.ValidationException;
import com.enterprise.payment.exception.DuplicateResourceException;
import com.enterprise.payment.repository.RefreshTokenRepository;
import com.enterprise.payment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for handling authentication operations including login, logout, and token management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService extends BaseService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;

    /**
     * Authenticate user and generate tokens
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        logMethodEntry("login", request.getUsernameOrEmail());
        
        validateRequired(request.getUsernameOrEmail(), "usernameOrEmail");
        validateRequired(request.getPassword(), "password");
        
        // Find user by username or email
        User user;
        if (request.isEmailLogin()) {
            user = userRepository.findByEmail(request.getUsernameOrEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));
        } else {
            user = userRepository.findByUsername(request.getUsernameOrEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));
        }
            
        // Check if account is locked
        if (!user.isAccountNonLocked()) {
            throw new AuthenticationException("Account is locked. Please try again later.");
        }
        
        // Check if account is active
        if (!user.getIsActive()) {
            throw new AuthenticationException("Account is inactive. Please contact support.");
        }
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            userService.recordLoginAttempt(user.getUsername(), false);
            throw new AuthenticationException("Invalid username or password");
        }
        
        // Record successful login
        userService.recordLoginAttempt(user.getUsername(), true);
        
        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);
        
        // Create response
        AuthResponse response = createAuthResponse(user, accessToken, refreshToken);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("userId", user.getId());
        metadata.put("role", user.getRole());
        
        auditLog("USER_LOGIN", "USER", user.getId(), 
                "User logged in successfully", metadata);
        
        log.info("User authenticated successfully: {}", user.getUsername());
        logMethodExit("login", "success");
        return response;
    }

    /**
     * Register new user
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        logMethodEntry("register", request.getUserInfo().getUsername());
        
        validateRequired(request.getUserInfo(), "userInfo");
        validateRequired(request.getUserInfo().getUsername(), "username");
        validateRequired(request.getUserInfo().getEmail(), "email");
        validateRequired(request.getUserInfo().getPassword(), "password");
        validateRequired(request.getUserInfo().getFirstName(), "firstName");
        validateRequired(request.getUserInfo().getLastName(), "lastName");
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUserInfo().getUsername())) {
            throw new ValidationException("Username is already taken");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getUserInfo().getEmail())) {
            throw new ValidationException("Email is already registered");
        }
        
        // Create user
        User user = createUserFromRegisterRequest(request);
        user = userRepository.save(user);
        
        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);
        
        // Create response
        AuthResponse response = createAuthResponse(user, accessToken, refreshToken);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("userId", user.getId());
        metadata.put("email", user.getEmail());
        
        auditLog("USER_REGISTERED", "USER", user.getId(), 
                "User registered successfully", metadata);
        
        log.info("User registered successfully: {}", user.getUsername());
        logMethodExit("register", "success");
        return response;
    }

    /**
     * Refresh access token using refresh token
     */
    @Transactional
    public AuthResponse refreshToken(String refreshTokenValue) {
        logMethodEntry("refreshToken", "***");
        
        validateRequired(refreshTokenValue, "refreshToken");
        
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
            .orElseThrow(() -> new AuthenticationException("Invalid refresh token"));
            
        // Check if refresh token is expired
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new AuthenticationException("Refresh token expired");
        }
        
        User user = refreshToken.getUser();
        
        // Check if user is still active
        if (!user.getIsActive()) {
            throw new AuthenticationException("User account is inactive");
        }
        
        // Generate new access token
        String newAccessToken = jwtService.generateAccessToken(user);
        
        // Update refresh token expiry (extend by 30 days)
        refreshToken.setExpiresAt(OffsetDateTime.now().plusDays(30));
        refreshTokenRepository.save(refreshToken);
        
        // Create response
        AuthResponse response = createAuthResponse(user, newAccessToken, refreshTokenValue);
        
        auditLog("TOKEN_REFRESHED", "USER", user.getId(), 
                "Access token refreshed successfully");
        
        log.info("Token refreshed for user: {}", user.getUsername());
        logMethodExit("refreshToken", "success");
        return response;
    }

    /**
     * Logout user and revoke tokens
     */
    @Transactional
    public void logout(String refreshTokenValue) {
        logMethodEntry("logout", "***");
        
        if (refreshTokenValue != null && !refreshTokenValue.trim().isEmpty()) {
            Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(refreshTokenValue);
            if (refreshToken.isPresent()) {
                User user = refreshToken.get().getUser();
                refreshTokenRepository.delete(refreshToken.get());
                
                auditLog("USER_LOGOUT", "USER", user.getId(), 
                        "User logged out successfully");
                
                log.info("User logged out: {}", user.getUsername());
            }
        }
        
        logMethodExit("logout");
    }

    /**
     * Revoke all refresh tokens for user
     */
    @Transactional
    public void revokeAllTokens(String username) {
        logMethodEntry("revokeAllTokens", username);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));
            
        refreshTokenRepository.deleteByUser(user);
        
        auditLog("ALL_TOKENS_REVOKED", "USER", user.getId(), 
                "All refresh tokens revoked");
        
        log.info("All tokens revoked for user: {}", username);
        logMethodExit("revokeAllTokens");
    }

    /**
     * Change user password
     */
    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        logMethodEntry("changePassword", username, "***", "***");
        
        validateRequired(currentPassword, "currentPassword");
        validateRequired(newPassword, "newPassword");
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));
            
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Current password is incorrect");
        }
        
        // Validate new password strength
        validatePasswordStrength(newPassword);
        
        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);
        
        // Revoke all existing refresh tokens to force re-login
        refreshTokenRepository.deleteByUser(user);
        
        auditLog("PASSWORD_CHANGED", "USER", user.getId(), 
                "Password changed successfully");
        
        log.info("Password changed for user: {}", username);
        logMethodExit("changePassword");
    }

    /**
     * Reset password using reset token
     */
    @Transactional
    public void resetPassword(String resetToken, String newPassword, String clientIp) {
        logMethodEntry("resetPassword", "***", "***", clientIp);
        
        validateRequired(resetToken, "resetToken");
        validateRequired(newPassword, "newPassword");
        
        // TODO: Implement password reset token validation
        // For now, throw an exception to indicate the method is not fully implemented
        throw new UnsupportedOperationException("Password reset functionality is not yet implemented");
        
        // Future implementation would:
        // 1. Validate the reset token
        // 2. Find the user associated with the token
        // 3. Check if token is not expired
        // 4. Validate new password strength
        // 5. Update user password
        // 6. Invalidate the reset token
        // 7. Log the password reset event
        
        // logMethodExit("resetPassword");
    }

    // Private helper methods

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


    private String generateRefreshToken(User user) {
        // Clean up expired tokens for this user
        refreshTokenRepository.deleteByUserAndExpiresAtBefore(user, OffsetDateTime.now());
        
        String tokenValue = UUID.randomUUID().toString();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(30); // 30 days expiry
        
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(tokenValue);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(expiresAt);
        refreshToken.setCreatedAt(OffsetDateTime.now());
        
        refreshTokenRepository.save(refreshToken);
        
        return tokenValue;
    }

    private User createUserFromRegisterRequest(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUserInfo().getUsername().trim());
        user.setEmail(request.getUserInfo().getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getUserInfo().getPassword()));
        user.setFirstName(request.getUserInfo().getFirstName().trim());
        user.setLastName(request.getUserInfo().getLastName().trim());
        user.setRole(request.getUserInfo().getRole() != null ? request.getUserInfo().getRole() : User.UserRole.USER);
        user.setIsActive(true);
        user.setFailedLoginAttempts(0);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        
        return user;
    }

    private AuthResponse createAuthResponse(User user, String accessToken, String refreshToken) {
        // Create UserResponse from User entity
        UserResponse userResponse = UserResponse.fromEntity(user);
        
        // Create AuthResponse with tokens and user data
        AuthResponse response = new AuthResponse(accessToken, refreshToken, userResponse);
        response.setExpiresIn(jwtService.getAccessTokenExpiration());
        response.setRefreshExpiresIn(30 * 24 * 60 * 60L); // 30 days in seconds
        
        return response;
    }
}