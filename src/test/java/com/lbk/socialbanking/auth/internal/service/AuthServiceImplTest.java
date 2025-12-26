package com.lbk.socialbanking.auth.internal.service;

import com.lbk.socialbanking.auth.internal.persistence.UserCredentialEntity;
import com.lbk.socialbanking.auth.internal.persistence.UserCredentialRepository;
import com.lbk.socialbanking.auth.web.LoginRequest;
import com.lbk.socialbanking.auth.web.RefreshRequest;
import com.lbk.socialbanking.auth.web.TokenResponse;
import com.lbk.socialbanking.common.api.ApiException;
import com.lbk.socialbanking.common.api.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserCredentialRepository credentialRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthEventPublisher authEventPublisher;

    @InjectMocks
    private AuthServiceImpl authService;

    @Nested
    @DisplayName("login tests")
    class LoginTests {

        @Test
        @DisplayName("should return TokenResponse when credentials are valid")
        void login_success() {
            String userId = "testuser";
            String pin = "123456";
            String hashedPin = "$2a$10$hashedpin";
            LoginRequest request = new LoginRequest(userId, pin);

            UserCredentialEntity credential = mock(UserCredentialEntity.class);
            when(credential.getUserId()).thenReturn(userId);
            when(credential.getSecretHash()).thenReturn(hashedPin);

            when(credentialRepository.findTopByUserIdOrderByUpdatedAtDesc(userId))
                    .thenReturn(Optional.of(credential));
            when(passwordEncoder.matches(pin, hashedPin)).thenReturn(true);
            when(jwtService.mintAccessToken(userId)).thenReturn("access-token");
            when(jwtService.mintRefreshToken(userId)).thenReturn("refresh-token");

            TokenResponse response = authService.login(request);

            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");
            assertThat(response.expiresInSeconds()).isEqualTo(900L);
            assertThat(response.tokenType()).isEqualTo("Bearer");

            verify(authEventPublisher).publishUserLoggedIn(userId);
        }

        @Test
        @DisplayName("should throw ApiException with INVALID_USER when user not found")
        void login_userNotFound() {
            String userId = "nonexistent";
            String pin = "123456";
            LoginRequest request = new LoginRequest(userId, pin);

            when(credentialRepository.findTopByUserIdOrderByUpdatedAtDesc(userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.UNAUTHORIZED)
                    .hasFieldOrPropertyWithValue("code", "INVALID_USER")
                    .hasFieldOrPropertyWithValue("message", "USER ID is incorrect.");
        }

        @Test
        @DisplayName("should throw ApiException with INVALID_PIN when pin is incorrect")
        void login_invalidPin() {
            String userId = "testuser";
            String pin = "123456";
            String hashedPin = "$2a$10$hashedpin";
            LoginRequest request = new LoginRequest(userId, pin);

            UserCredentialEntity credential = mock(UserCredentialEntity.class);
            when(credential.getSecretHash()).thenReturn(hashedPin);

            when(credentialRepository.findTopByUserIdOrderByUpdatedAtDesc(userId))
                    .thenReturn(Optional.of(credential));
            when(passwordEncoder.matches(pin, hashedPin)).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.UNAUTHORIZED)
                    .hasFieldOrPropertyWithValue("code", "INVALID_PIN")
                    .hasFieldOrPropertyWithValue("message", "Invalid credentials.");
        }

        @Test
        @DisplayName("should call repository with correct userId")
        void login_verifyRepositoryCall() {
            // Given
            String userId = "specificUserId";
            String pin = "123456";
            String hashedPin = "$2a$10$hashedpin";
            LoginRequest request = new LoginRequest(userId, pin);

            UserCredentialEntity credential = mock(UserCredentialEntity.class);
            when(credential.getSecretHash()).thenReturn(hashedPin);

            when(credentialRepository.findTopByUserIdOrderByUpdatedAtDesc(userId))
                    .thenReturn(Optional.of(credential));
            when(passwordEncoder.matches(pin, hashedPin)).thenReturn(true);
            when(jwtService.mintAccessToken(userId)).thenReturn("access-token");
            when(jwtService.mintRefreshToken(userId)).thenReturn("refresh-token");

            authService.login(request);
        }
    }

    @Nested
    @DisplayName("refresh tests")
    class RefreshTests {

        @Test
        @DisplayName("should return new TokenResponse when refresh token is valid")
        void refresh_success() {
            String refreshToken = "valid-refresh-token";
            String userId = "testuser";
            RefreshRequest request = new RefreshRequest(refreshToken);

            JwtService.JwtParsed parsedToken = new JwtService.JwtParsed(userId, "refresh");

            when(jwtService.parseAndValidate(refreshToken)).thenReturn(parsedToken);
            when(jwtService.mintAccessToken(userId)).thenReturn("new-access-token");
            when(jwtService.mintRefreshToken(userId)).thenReturn("new-refresh-token");

            TokenResponse response = authService.refresh(request);

            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo("new-access-token");
            assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
            assertThat(response.expiresInSeconds()).isEqualTo(900L);
            assertThat(response.tokenType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("should throw ApiException when token type is not refresh")
        void refresh_invalidTokenType() {
            String accessToken = "access-token-not-refresh";
            String userId = "testuser";
            RefreshRequest request = new RefreshRequest(accessToken);

            JwtService.JwtParsed parsedToken = new JwtService.JwtParsed(userId, "access");

            when(jwtService.parseAndValidate(accessToken)).thenReturn(parsedToken);

            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
                    .hasFieldOrPropertyWithValue("code", "INVALID_REQUEST")
                    .hasFieldOrPropertyWithValue("message", "Invalid token type");
        }

        @Test
        @DisplayName("should throw ApiException when token type is null")
        void refresh_nullTokenType() {
            String refreshToken = "token-with-null-type";
            String userId = "testuser";
            RefreshRequest request = new RefreshRequest(refreshToken);

            JwtService.JwtParsed parsedToken = new JwtService.JwtParsed(userId, null);

            when(jwtService.parseAndValidate(refreshToken)).thenReturn(parsedToken);

            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
                    .hasFieldOrPropertyWithValue("code", "INVALID_REQUEST")
                    .hasFieldOrPropertyWithValue("message", "Invalid token type");
        }

        @Test
        @DisplayName("should generate tokens for correct userId from refresh token")
        void refresh_correctUserId() {
            String refreshToken = "valid-refresh-token";
            String userId = "specificUserId";
            RefreshRequest request = new RefreshRequest(refreshToken);

            JwtService.JwtParsed parsedToken = new JwtService.JwtParsed(userId, "refresh");

            when(jwtService.parseAndValidate(refreshToken)).thenReturn(parsedToken);
            when(jwtService.mintAccessToken(userId)).thenReturn("new-access-token");
            when(jwtService.mintRefreshToken(userId)).thenReturn("new-refresh-token");

            authService.refresh(request);

            verify(jwtService).mintAccessToken(userId);
            verify(jwtService).mintRefreshToken(userId);
        }

        @Test
        @DisplayName("should handle malformed or expired token from jwtService")
        void refresh_malformedToken() {
            String malformedToken = "malformed-token";
            RefreshRequest request = new RefreshRequest(malformedToken);

            when(jwtService.parseAndValidate(malformedToken))
                    .thenThrow(new RuntimeException("Token validation failed"));

            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Token validation failed");

            verify(jwtService).parseAndValidate(malformedToken);
            verify(jwtService, never()).mintAccessToken(anyString());
            verify(jwtService, never()).mintRefreshToken(anyString());
        }
    }
}
