package com.sentinel.auth.service;

import com.sentinel.audit.service.AuditService;
import com.sentinel.auth.dto.AuthResponse;
import com.sentinel.auth.dto.LoginRequest;
import com.sentinel.auth.dto.RefreshTokenRequest;
import com.sentinel.auth.dto.RegisterRequest;
import com.sentinel.auth.entity.RefreshToken;
import com.sentinel.auth.repository.RefreshTokenRepository;
import com.sentinel.common.enums.UserRole;
import com.sentinel.common.exception.DuplicateResourceException;
import com.sentinel.common.exception.UnauthorizedException;
import com.sentinel.security.JwtTokenProvider;
import com.sentinel.user.entity.User;
import com.sentinel.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("dev@sentinel.io", "hashedPass123", "Test Developer", UserRole.DEVELOPER);
        sampleUser.setId(10L);
        sampleUser.setActive(true);
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void testRegisterSuccess() {
        RegisterRequest req = new RegisterRequest("New User", "new@sentinel.io", "Password@123");

        when(userRepository.existsByEmail("new@sentinel.io")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(20L);
            return u;
        });
        when(jwtTokenProvider.generateAccessToken(any(), any(), any())).thenReturn("mockAccessJwt");
        when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("mockRefreshJwt");
        when(jwtTokenProvider.getRefreshTokenExpiration()).thenReturn(604800000L);

        AuthResponse response = authService.register(req);

        assertNotNull(response);
        assertEquals("mockAccessJwt", response.getAccessToken());
        assertEquals("mockRefreshJwt", response.getRefreshToken());
        assertEquals("new@sentinel.io", response.getUser().getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException if email is already taken")
    void testRegisterDuplicateEmail() {
        RegisterRequest req = new RegisterRequest("Existing User", "dev@sentinel.io", "Password@123");
        when(userRepository.existsByEmail("dev@sentinel.io")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLoginSuccess() {
        LoginRequest req = new LoginRequest("dev@sentinel.io", "Password@123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("dev@sentinel.io", null));
        when(userRepository.findByEmail("dev@sentinel.io")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(jwtTokenProvider.generateAccessToken(10L, "dev@sentinel.io", "DEVELOPER")).thenReturn("mockAccessJwt");
        when(jwtTokenProvider.generateRefreshToken("dev@sentinel.io")).thenReturn("mockRefreshJwt");
        when(jwtTokenProvider.getRefreshTokenExpiration()).thenReturn(604800000L);

        AuthResponse response = authService.login(req);

        assertNotNull(response);
        assertEquals("mockAccessJwt", response.getAccessToken());
        assertEquals(10L, response.getUser().getId());
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when login credentials are invalid")
    void testLoginBadCredentials() {
        LoginRequest req = new LoginRequest("dev@sentinel.io", "WrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        assertThrows(BadCredentialsException.class, () -> authService.login(req));
    }

    @Test
    @DisplayName("Should revoke tokens on logout")
    void testLogout() {
        authService.logout(10L);
        verify(refreshTokenRepository).revokeAllByUserId(10L);
    }
}

