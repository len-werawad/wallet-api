package com.lbk.wallet.auth.web;

import com.lbk.wallet.auth.internal.service.AuthService;
import com.lbk.wallet.common.api.ApiException;
import com.lbk.wallet.common.api.JwtService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthService authService;

    @Nested
    @DisplayName("POST /v1/auth/login/pin")
    class LoginTests {
        private MockHttpServletRequestBuilder requestBuilder(LoginRequest request) throws JsonProcessingException {
            return post("/v1/auth/login/pin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request));
        }

        private MockHttpServletRequestBuilder requestBuilder(String rawJson) {
            return post("/v1/auth/login/pin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(rawJson);
        }

        private void expectResponseError(ResultActions actions, HttpStatus status, String code, String message) throws Exception {
            actions.andExpect(status().is(status.value()))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.error.status").value(status.value()))
                    .andExpect(jsonPath("$.error.code").value(code))
                    .andExpect(jsonPath("$.error.message").value(message));
        }

        @Test
        @DisplayName("login success should return 200 and token")
        void login_success() throws Exception {
            LoginRequest request = new LoginRequest("testuser", "123456");

            TokenResponse response = new TokenResponse(
                    "access-token-123",
                    "refresh-token-456",
                    900L,
                    "Bearer"
            );

            when(authService.login(request)).thenReturn(response);

            mockMvc.perform(requestBuilder(request))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.accessToken").value("access-token-123"))
                    .andExpect(jsonPath("$.data.refreshToken").value("refresh-token-456"))
                    .andExpect(jsonPath("$.data.expiresInSeconds").value(900))
                    .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
        }

        @Test
        @DisplayName("login with invalid user should return 401")
        void login_invalidUser() throws Exception {
            LoginRequest request = new LoginRequest("wronguser", "123456");

            when(authService.login(request))
                    .thenThrow(new ApiException(
                            HttpStatus.UNAUTHORIZED,
                            "INVALID_USER",
                            "USER ID is incorrect."
                    ));

            var actionResult = mockMvc.perform(requestBuilder(request));

            expectResponseError(actionResult, HttpStatus.UNAUTHORIZED, "INVALID_USER", "USER ID is incorrect.");
        }

        @Test
        @DisplayName("login with invalid pin should return 401")
        void login_invalidPin() throws Exception {
            LoginRequest request = new LoginRequest("testuser", "000000");

            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new ApiException(
                            HttpStatus.UNAUTHORIZED,
                            "INVALID_PIN",
                            "Invalid credentials."
                    ));

            var actionResult = mockMvc.perform(requestBuilder(request));

            expectResponseError(actionResult, HttpStatus.UNAUTHORIZED, "INVALID_PIN", "Invalid credentials.");
        }

        @Test
        @DisplayName("login with blank userId should return 400")
        void login_blankUserId() throws Exception {
            String requestJson = """
                    {
                      "userId": "",
                      "pin": "123456"
                    }
                    """;

            var actionResult = mockMvc.perform(requestBuilder(requestJson));

            expectResponseError(actionResult, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "userId is required");
        }
    }

    @Nested
    @DisplayName("POST /v1/auth/refresh")
    class RefreshTests {

        private MockHttpServletRequestBuilder requestBuilder(RefreshRequest request) throws JsonProcessingException {
            return post("/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request));
        }

        private MockHttpServletRequestBuilder requestBuilder(String rawJson) {
            return post("/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(rawJson);
        }

        private void expectResponseError(ResultActions actions, HttpStatus status, String code, String message) throws Exception {
            actions.andExpect(status().is(status.value()))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.error.status").value(status.value()))
                    .andExpect(jsonPath("$.error.code").value(code))
                    .andExpect(jsonPath("$.error.message").value(message));
        }

        @Test
        @DisplayName("refresh token success should return 200 and new tokens")
        void refresh_success() throws Exception {
            RefreshRequest request = new RefreshRequest("valid-refresh-token");

            TokenResponse response = new TokenResponse(
                    "new-access-token",
                    "new-refresh-token",
                    900L,
                    "Bearer"
            );

            when(authService.refresh(request)).thenReturn(response);

            mockMvc.perform(requestBuilder(request))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                    .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"))
                    .andExpect(jsonPath("$.data.expiresInSeconds").value(900))
                    .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
        }

        @Test
        @DisplayName("refresh with invalid token type should return 400")
        void refresh_invalidTokenType() throws Exception {
            RefreshRequest request = new RefreshRequest("access-token-not-refresh");

            when(authService.refresh(request))
                    .thenThrow(new ApiException(
                            HttpStatus.BAD_REQUEST,
                            "INVALID_REQUEST",
                            "Invalid token type"
                    ));

            var actionResult = mockMvc.perform(requestBuilder(request));

            expectResponseError(actionResult, HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Invalid token type");
        }

        @Test
        @DisplayName("refresh with expired token should return 401")
        void refresh_expiredToken() throws Exception {
            RefreshRequest request = new RefreshRequest("expired-refresh-token");

            when(authService.refresh(request))
                    .thenThrow(new ApiException(
                            HttpStatus.UNAUTHORIZED,
                            "TOKEN_EXPIRED",
                            "Token has expired"
                    ));

            var actionResult = mockMvc.perform(requestBuilder(request));

            expectResponseError(actionResult, HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "Token has expired");
        }

        @Test
        @DisplayName("refresh with blank refreshToken should return 400")
        void refresh_blankToken() throws Exception {
            String requestJson = """
                    {
                      "refreshToken": ""
                    }
                    """;

            var actionResult = mockMvc.perform(requestBuilder(requestJson));

            expectResponseError(actionResult, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "refreshToken is required");
        }

        @Test
        @DisplayName("refresh token with missing refreshToken should return 400")
        void refresh_missingToken() throws Exception {
            var actionResult = mockMvc.perform(requestBuilder("{}"));

            expectResponseError(actionResult, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "refreshToken is required");
        }

        @Test
        @DisplayName("refresh token with null refreshToken should return 400")
        void refresh_nullToken() throws Exception {
            String requestJson = """
                    {
                      "refreshToken": null
                    }
                    """;
            var actionResult = mockMvc.perform(requestBuilder(requestJson));

            expectResponseError(actionResult, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "refreshToken is required");
        }
    }
}
