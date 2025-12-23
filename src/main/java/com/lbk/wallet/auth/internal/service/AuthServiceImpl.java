package com.lbk.wallet.auth.internal.service;

import com.lbk.wallet.auth.internal.persistence.UserCredentialRepository;
import com.lbk.wallet.auth.web.LoginRequest;
import com.lbk.wallet.auth.web.RefreshRequest;
import com.lbk.wallet.auth.web.TokenResponse;
import com.lbk.wallet.common.api.ApiException;
import com.lbk.wallet.common.api.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserCredentialRepository credentials;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthEventPublisher authEventPublisher;

    AuthServiceImpl(UserCredentialRepository credentials, PasswordEncoder passwordEncoder, JwtService jwtService,
                    AuthEventPublisher authEventPublisher) {
        this.credentials = credentials;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authEventPublisher = authEventPublisher;
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.userId());

        var cred = credentials.findTopByUserIdOrderByUpdatedAtDesc(request.userId())
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found: {}", request.userId());
                    return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_USER", "USER ID is incorrect.");
                });

        if (!passwordEncoder.matches(request.pin(), cred.getSecretHash())) {
            log.warn("Login failed - invalid PIN for user: {}", request.userId());
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_PIN", "Invalid credentials.");
        }

        log.info("Login successful for user: {}", request.userId());

        // Publish login event
        authEventPublisher.publishUserLoggedIn(cred.getUserId());

        return new TokenResponse(jwtService.mintAccessToken(request.userId()), jwtService.mintRefreshToken(request.userId()), 900, "Bearer");
    }

    @Override
    public TokenResponse refresh(RefreshRequest request) {
        log.debug("Token refresh requested");

        var parsed = jwtService.parseAndValidate(request.refreshToken());
        if (!"refresh".equals(parsed.typ())) {
            log.warn("Token refresh failed - invalid token type: {}", parsed.typ());
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Invalid token type");
        }

        log.info("Token refresh successful for user: {}", parsed.userId());
        return new TokenResponse(jwtService.mintAccessToken(parsed.userId()), jwtService.mintRefreshToken(parsed.userId()), 900, "Bearer");
    }
}
