package com.enterprise.payment.controller;

import com.enterprise.payment.dto.request.LoginRequest;
import com.enterprise.payment.dto.request.RegisterRequest;
import com.enterprise.payment.dto.response.ApiResponse;
import com.enterprise.payment.dto.response.AuthResponse;
import com.enterprise.payment.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * Handles user authentication operations including login, registration, token refresh, and logout
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController extends BaseController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "User login", description = "Authenticate user with email and password. Returns JWT tokens for API access.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Login attempt for email: {}", request.getUsernameOrEmail());
        
        try {
            String clientIp = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            // Set client information in request
            request.setIpAddress(clientIp);
            request.setUserAgent(userAgent);
            
            AuthResponse authResponse = authenticationService.login(request);
            
            log.info("Login successful for user: {}", authResponse.getUser().getEmail());
            return success(authResponse, "Login successful");
            
        } catch (Exception e) {
            log.error("Login failed for email: {}", request.getUsernameOrEmail(), e);
            return unauthorized("Invalid credentials");
        }
    }

    @Operation(summary = "User registration", description = "Register a new user account. Email verification may be required.")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Registration attempt for email: {}", request.getUserInfo().getEmail());
        
        try {
            String clientIp = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            AuthResponse authResponse = authenticationService.register(request);
            
            log.info("Registration successful for user: {}", authResponse.getUser().getEmail());
            return created(authResponse, "Registration successful");
            
        } catch (Exception e) {
            log.error("Registration failed for email: {}", request.getUserInfo().getEmail(), e);
            if (e.getMessage().contains("already exists")) {
                return conflict("Email address already exists");
            }
            return badRequest("Registration failed: " + e.getMessage());
        }
    }

    @Operation(summary = "Refresh access token", description = "Generate a new access token using a valid refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Parameter(description = "Refresh token", required = true)
            @RequestHeader("Authorization") String refreshToken,
            HttpServletRequest httpRequest) {
        
        log.info("Token refresh attempt");
        
        try {
            // Remove "Bearer " prefix if present
            String token = refreshToken.startsWith("Bearer ") ? 
                    refreshToken.substring(7) : refreshToken;
            
            String clientIp = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            AuthResponse authResponse = authenticationService.refreshToken(token);
            
            log.info("Token refresh successful for user: {}", authResponse.getUser().getEmail());
            return success(authResponse, "Token refreshed successfully");
            
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return unauthorized("Invalid or expired refresh token");
        }
    }

    @Operation(summary = "Logout user", description = "Invalidate the user's access and refresh tokens", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(description = "Access token", required = true)
            @RequestHeader("Authorization") String accessToken,
            HttpServletRequest httpRequest) {
        
        log.info("Logout attempt for user: {}", getCurrentUserId());
        
        try {
            // Remove "Bearer " prefix if present
            String token = accessToken.startsWith("Bearer ") ? 
                    accessToken.substring(7) : accessToken;
            
            String clientIp = getClientIpAddress(httpRequest);
            
            authenticationService.logout(token);
            
            log.info("Logout successful for user: {}", getCurrentUserId());
            return success(null, "Logout successful");
            
        } catch (Exception e) {
            log.error("Logout failed for user: {}", getCurrentUserId(), e);
            return internalServerError("Logout failed");
        }
    }

    @Operation(summary = "Logout from all devices", description = "Invalidate all active sessions for the current user", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/logout-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logoutAll(HttpServletRequest httpRequest) {
        
        log.info("Logout all devices attempt for user: {}", getCurrentUserId());
        
        try {
            String clientIp = getClientIpAddress(httpRequest);
            
            authenticationService.revokeAllTokens(getCurrentUserId());
            
            log.info("Logout all devices successful for user: {}", getCurrentUserId());
            return success(null, "Logged out from all devices successfully");
            
        } catch (Exception e) {
            log.error("Logout all devices failed for user: {}", getCurrentUserId(), e);
            return internalServerError("Logout from all devices failed");
        }
    }

    @Operation(summary = "Verify email address", description = "Verify user's email address using verification token")
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Parameter(description = "Email verification token", required = true)
            @RequestParam String token) {
        
        log.info("Email verification attempt with token: {}", token.substring(0, Math.min(10, token.length())));
        
        // TODO: Implement email verification in AuthenticationService
        return success(null, "Email verification feature not yet implemented");
    }

    @Operation(summary = "Request password reset", description = "Send password reset email to the specified email address")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Parameter(description = "Email address", required = true)
            @RequestParam String email,
            HttpServletRequest httpRequest) {
        
        log.info("Password reset request for email: {}", email);
        
        try {
            // TODO: Implement password reset in AuthenticationService
            log.info("Password reset email would be sent to: {}", email);
            return success(null, "Password reset email sent if the email address exists");
            
        } catch (Exception e) {
            log.error("Password reset request failed for email: {}", email, e);
            // Always return success to prevent email enumeration
            return success(null, "Password reset email sent if the email address exists");
        }
    }

    @Operation(summary = "Reset password", description = "Reset user password using reset token")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Parameter(description = "Password reset token", required = true)
            @RequestParam String token,
            @Parameter(description = "New password", required = true)
            @RequestParam String newPassword,
            HttpServletRequest httpRequest) {
        
        log.info("Password reset attempt with token: {}", token.substring(0, Math.min(10, token.length())));
        
        try {
            String clientIp = getClientIpAddress(httpRequest);
            
            authenticationService.resetPassword(token, newPassword, clientIp);
            
            log.info("Password reset successful");
            return success(null, "Password reset successful");
            
        } catch (Exception e) {
            log.error("Password reset failed", e);
            return badRequest("Invalid or expired reset token");
        }
    }

    @Operation(summary = "Validate token", description = "Validate if the provided token is valid and not expired", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/validate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> validateToken() {
        
        log.info("Token validation for user: {}", getCurrentUserId());
        
        return success(null, "Token is valid");
    }

    /**
     * Extract client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}