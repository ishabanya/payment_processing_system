package com.enterprise.payment.service;

import com.enterprise.payment.dto.request.LoginRequest;
import com.enterprise.payment.dto.request.RegisterRequest;
import com.enterprise.payment.dto.response.AuthResponse;
import com.enterprise.payment.entity.RefreshToken;
import com.enterprise.payment.entity.User;
import com.enterprise.payment.exception.DuplicateResourceException;
import com.enterprise.payment.exception.ValidationException;
import com.enterprise.payment.repository.RefreshTokenRepository;
import com.enterprise.payment.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private RegisterRequest.UserInfo userInfo;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(User.UserRole.USER);
        testUser.setIsActive(true);

        loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("testuser");
        loginRequest.setPassword("password123");

        userInfo = new RegisterRequest.UserInfo();
        userInfo.setUsername("newuser");
        userInfo.setEmail("newuser@example.com");
        userInfo.setFirstName("New");
        userInfo.setLastName("User");
        userInfo.setPassword("password123");
        userInfo.setConfirmPassword("password123");

        registerRequest = new RegisterRequest();
        registerRequest.setUserInfo(userInfo);
        registerRequest.setAcceptTerms(true);
        registerRequest.setAcceptPrivacy(true);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("testuser", "testuser"))
                .thenReturn(Optional.of(testUser));

        // Act
        AuthResponse response = authenticationService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertNotNull(response.getUser());
        assertEquals("testuser", response.getUser().getUsername());

        verify(userRepository).findByUsernameOrEmail("testuser", "testuser");
    }

    @Test
    void login_WithInvalidCredentials_ShouldThrowValidationException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
                () -> authenticationService.login(loginRequest));
        
        assertEquals("Invalid username/email or password", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(userRepository, jwtService);
    }

    @Test
    void login_WithUserNotFound_ShouldThrowValidationException() {
        // Arrange
        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(userRepository.findByUsernameOrEmail("testuser", "testuser"))
                .thenReturn(Optional.empty());

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
                () -> authenticationService.login(loginRequest));
        
        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByUsernameOrEmail("testuser", "testuser");
    }

    @Test
    void register_WithValidData_ShouldReturnAuthResponse() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(testUser)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("refresh-token");

        // Act
        AuthResponse response = authenticationService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getType());
        assertNotNull(response.getUser());

        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("newuser@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(testUser);
        verify(jwtService).generateRefreshToken(testUser);
    }

    @Test
    void register_WithExistingUsername_ShouldThrowDuplicateResourceException() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, 
                () -> authenticationService.register(registerRequest));
        
        assertEquals("Username 'newuser' is already taken", exception.getMessage());
        verify(userRepository).existsByUsername("newuser");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void register_WithExistingEmail_ShouldThrowDuplicateResourceException() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, 
                () -> authenticationService.register(registerRequest));
        
        assertEquals("Email 'newuser@example.com' is already registered", exception.getMessage());
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("newuser@example.com");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void logout_ShouldDeleteRefreshTokens() {
        // Act
        authenticationService.logout(testUser);

        // Assert
        verify(refreshTokenRepository).deleteByUser(testUser);
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewTokens() {
        // Arrange
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(testUser);
        refreshToken.setToken("valid-refresh-token");
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(refreshTokenRepository.findByToken("valid-refresh-token"))
                .thenReturn(Optional.of(refreshToken));
        when(jwtService.generateToken(testUser)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("new-refresh-token");

        // Act
        AuthResponse response = authenticationService.refreshToken("valid-refresh-token");

        // Assert
        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getType());

        verify(refreshTokenRepository).findByToken("valid-refresh-token");
        verify(refreshTokenRepository).delete(refreshToken);
        verify(jwtService).generateToken(testUser);
        verify(jwtService).generateRefreshToken(testUser);
    }

    @Test
    void refreshToken_WithInvalidToken_ShouldThrowValidationException() {
        // Arrange
        when(refreshTokenRepository.findByToken("invalid-token"))
                .thenReturn(Optional.empty());

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
                () -> authenticationService.refreshToken("invalid-token"));
        
        assertEquals("Invalid refresh token", exception.getMessage());
        verify(refreshTokenRepository).findByToken("invalid-token");
        verifyNoInteractions(jwtService);
    }

    @Test
    void refreshToken_WithExpiredToken_ShouldThrowValidationException() {
        // Arrange
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setUser(testUser);
        expiredToken.setToken("expired-token");
        expiredToken.setExpiresAt(LocalDateTime.now().minusDays(1));

        when(refreshTokenRepository.findByToken("expired-token"))
                .thenReturn(Optional.of(expiredToken));

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
                () -> authenticationService.refreshToken("expired-token"));
        
        assertEquals("Refresh token has expired", exception.getMessage());
        verify(refreshTokenRepository).findByToken("expired-token");
        verify(refreshTokenRepository).delete(expiredToken);
        verifyNoInteractions(jwtService);
    }

    @Test
    void cleanupExpiredTokens_ShouldDeleteExpiredTokens() {
        // Act
        authenticationService.cleanupExpiredTokens();

        // Assert
        verify(refreshTokenRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }
}