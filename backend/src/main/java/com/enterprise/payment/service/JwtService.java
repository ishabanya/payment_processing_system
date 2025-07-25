package com.enterprise.payment.service;

import com.enterprise.payment.entity.User;
import com.enterprise.payment.exception.AuthenticationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service for JWT token creation, validation, and refresh
 */
@Service
@Slf4j
public class JwtService extends BaseService {

    @Value("${app.jwt.secret:mySecretKey12345678901234567890123456789012345678901234567890}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration:3600000}") // 1 hour in milliseconds
    private Long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration:2592000000}") // 30 days in milliseconds
    private Long refreshTokenExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generate access token for user
     */
    public String generateAccessToken(User user) {
        logMethodEntry("generateAccessToken", user.getUsername());
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().toString());
        claims.put("accountId", user.getAccount() != null ? user.getAccount().getId() : null);
        claims.put("fullName", user.getFullName());
        
        String token = createToken(claims, user.getUsername(), accessTokenExpiration);
        
        auditLog("ACCESS_TOKEN_GENERATED", "USER", user.getId(), 
                "Access token generated");
        
        log.debug("Access token generated for user: {}", user.getUsername());
        logMethodExit("generateAccessToken", "token_generated");
        return token;
    }

    /**
     * Generate access token with custom claims
     */
    public String generateAccessToken(User user, Map<String, Object> extraClaims) {
        logMethodEntry("generateAccessToken", user.getUsername(), extraClaims);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().toString());
        claims.put("accountId", user.getAccount() != null ? user.getAccount().getId() : null);
        claims.put("fullName", user.getFullName());
        
        if (extraClaims != null) {
            claims.putAll(extraClaims);
        }
        
        String token = createToken(claims, user.getUsername(), accessTokenExpiration);
        
        auditLog("ACCESS_TOKEN_GENERATED", "USER", user.getId(), 
                "Access token generated with custom claims");
        
        log.debug("Access token generated with custom claims for user: {}", user.getUsername());
        logMethodExit("generateAccessToken", "token_generated");
        return token;
    }

    /**
     * Extract username from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract user ID from token
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Extract user role from token
     */
    public String extractUserRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extract account ID from token
     */
    public Long extractAccountId(String token) {
        return extractClaim(token, claims -> claims.get("accountId", Long.class));
    }

    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract specific claim from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (Exception e) {
            log.error("Failed to extract claim from token", e);
            throw new AuthenticationException("Invalid token");
        }
    }

    /**
     * Validate token against user details
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return false;
        }
    }

    /**
     * Validate token
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            log.debug("Token expired: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.error("Token validation failed", e);
            return false;
        }
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            log.error("Failed to check token expiration", e);
            return true;
        }
    }

    /**
     * Get access token expiration time in seconds
     */
    public long getAccessTokenExpiration() {
        return accessTokenExpiration / 1000; // Convert to seconds
    }

    /**
     * Get refresh token expiration time in seconds
     */
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration / 1000; // Convert to seconds
    }

    /**
     * Get token creation date
     */
    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    /**
     * Get remaining token validity time in seconds
     */
    public long getTokenRemainingValidity(String token) {
        try {
            Date expiration = extractExpiration(token);
            Date now = new Date();
            
            if (expiration.before(now)) {
                return 0;
            }
            
            return (expiration.getTime() - now.getTime()) / 1000;
        } catch (Exception e) {
            log.error("Failed to get token remaining validity", e);
            return 0;
        }
    }

    /**
     * Check if token needs refresh (less than 15 minutes remaining)
     */
    public boolean shouldRefreshToken(String token) {
        long remainingValidity = getTokenRemainingValidity(token);
        return remainingValidity < 900; // 15 minutes in seconds
    }

    /**
     * Extract all claims from token and validate signature
     */
    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.debug("Token expired", e);
            throw new AuthenticationException("Token expired");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token", e);
            throw new AuthenticationException("Unsupported token");
        } catch (MalformedJwtException e) {
            log.error("Malformed JWT token", e);
            throw new AuthenticationException("Malformed token");
        } catch (SignatureException e) {
            log.error("Invalid JWT signature", e);
            throw new AuthenticationException("Invalid token signature");
        } catch (IllegalArgumentException e) {
            log.error("JWT token compact of handler are invalid", e);
            throw new AuthenticationException("Invalid token");
        }
    }

    /**
     * Create JWT token with claims
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Invalidate token (for logout)
     * Note: In a production system, you might want to maintain a blacklist of invalidated tokens
     */
    public void invalidateToken(String token) {
        logMethodEntry("invalidateToken", "***");
        
        try {
            String username = extractUsername(token);
            Long userId = extractUserId(token);
            
            auditLog("TOKEN_INVALIDATED", "USER", userId, 
                    "Token invalidated for user: " + username);
            
            log.info("Token invalidated for user: {}", username);
        } catch (Exception e) {
            log.warn("Failed to extract user info from token during invalidation", e);
        }
        
        logMethodExit("invalidateToken");
    }

    /**
     * Get token type (always Bearer for JWT)
     */
    public String getTokenType() {
        return "Bearer";
    }

    /**
     * Extract custom claim
     */
    public Object extractCustomClaim(String token, String claimName) {
        return extractClaim(token, claims -> claims.get(claimName));
    }
}