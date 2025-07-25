package com.enterprise.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    @JsonProperty("accessToken")
    private String accessToken;

    @JsonProperty("refreshToken")
    private String refreshToken;

    @JsonProperty("tokenType")
    private String tokenType = "Bearer";

    @JsonProperty("expiresIn")
    private Long expiresIn; // in seconds

    @JsonProperty("expiresAt")
    private OffsetDateTime expiresAt;

    @JsonProperty("refreshExpiresIn")
    private Long refreshExpiresIn; // in seconds

    @JsonProperty("refreshExpiresAt")
    private OffsetDateTime refreshExpiresAt;

    @JsonProperty("user")
    private UserResponse user;

    @JsonProperty("permissions")
    private List<String> permissions;

    @JsonProperty("scopes")
    private List<String> scopes;

    // Authentication session information
    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("loginTime")
    private OffsetDateTime loginTime;

    @JsonProperty("lastActivity")
    private OffsetDateTime lastActivity;

    @JsonProperty("ipAddress")
    private String ipAddress;

    @JsonProperty("userAgent")
    private String userAgent;

    // Account/tenant information
    @JsonProperty("accountContext")
    private AccountContext accountContext;

    // Security information
    @JsonProperty("isFirstLogin")
    private Boolean isFirstLogin;

    @JsonProperty("passwordExpiresAt")
    private OffsetDateTime passwordExpiresAt;

    @JsonProperty("requiresPasswordChange")
    private Boolean requiresPasswordChange;

    @JsonProperty("requiresMfaSetup")
    private Boolean requiresMfaSetup;

    @JsonProperty("mfaEnabled")
    private Boolean mfaEnabled;

    // Constructor for successful authentication
    public AuthResponse(String accessToken, String refreshToken, UserResponse user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
        this.loginTime = OffsetDateTime.now();
        this.tokenType = "Bearer";
    }

    // Constructor with expiration times
    public AuthResponse(String accessToken, String refreshToken, UserResponse user, 
                       Long expiresIn, Long refreshExpiresIn) {
        this(accessToken, refreshToken, user);
        this.expiresIn = expiresIn;
        this.refreshExpiresIn = refreshExpiresIn;
        
        if (expiresIn != null) {
            this.expiresAt = OffsetDateTime.now().plusSeconds(expiresIn);
        }
        
        if (refreshExpiresIn != null) {
            this.refreshExpiresAt = OffsetDateTime.now().plusSeconds(refreshExpiresIn);
        }
    }

    // Static factory methods
    public static AuthResponse success(String accessToken, String refreshToken, UserResponse user) {
        return new AuthResponse(accessToken, refreshToken, user);
    }

    public static AuthResponse success(String accessToken, String refreshToken, UserResponse user,
                                     Long expiresIn, Long refreshExpiresIn) {
        return new AuthResponse(accessToken, refreshToken, user, expiresIn, refreshExpiresIn);
    }

    // Builder methods
    public AuthResponse withPermissions(List<String> permissions) {
        this.permissions = permissions;
        return this;
    }

    public AuthResponse withScopes(List<String> scopes) {
        this.scopes = scopes;
        return this;
    }

    public AuthResponse withSessionInfo(String sessionId, String ipAddress, String userAgent) {
        this.sessionId = sessionId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.lastActivity = OffsetDateTime.now();
        return this;
    }

    public AuthResponse withAccountContext(AccountContext accountContext) {
        this.accountContext = accountContext;
        return this;
    }

    public AuthResponse withSecurityInfo(Boolean isFirstLogin, Boolean requiresPasswordChange, 
                                       Boolean requiresMfaSetup, Boolean mfaEnabled) {
        this.isFirstLogin = isFirstLogin;
        this.requiresPasswordChange = requiresPasswordChange;
        this.requiresMfaSetup = requiresMfaSetup;
        this.mfaEnabled = mfaEnabled;
        return this;
    }

    // Check if token is expired
    public boolean isAccessTokenExpired() {
        return expiresAt != null && expiresAt.isBefore(OffsetDateTime.now());
    }

    public boolean isRefreshTokenExpired() {
        return refreshExpiresAt != null && refreshExpiresAt.isBefore(OffsetDateTime.now());
    }

    // Get remaining time in seconds
    public Long getRemainingAccessTokenTime() {
        if (expiresAt == null) return null;
        
        long remaining = java.time.Duration.between(OffsetDateTime.now(), expiresAt).getSeconds();
        return Math.max(0, remaining);
    }

    public Long getRemainingRefreshTokenTime() {
        if (refreshExpiresAt == null) return null;
        
        long remaining = java.time.Duration.between(OffsetDateTime.now(), refreshExpiresAt).getSeconds();
        return Math.max(0, remaining);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AccountContext {
        @JsonProperty("accountId")
        private Long accountId;

        @JsonProperty("accountNumber")
        private String accountNumber;

        @JsonProperty("accountName")
        private String accountName;

        @JsonProperty("accountStatus")
        private com.enterprise.payment.entity.Account.AccountStatus accountStatus;

        @JsonProperty("currencyCode")
        private String currencyCode;

        @JsonProperty("balance")
        private java.math.BigDecimal balance;

        @JsonProperty("tier")
        private String tier;

        @JsonProperty("features")
        private List<String> features;
    }
}