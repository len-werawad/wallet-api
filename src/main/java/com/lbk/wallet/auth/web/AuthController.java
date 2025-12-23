package com.lbk.wallet.auth.web;

import com.lbk.wallet.auth.internal.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for authentication operations")
public class AuthController {

    private final AuthService facade;

    public AuthController(AuthService facade) {
        this.facade = facade;
    }

    @PostMapping("/login/pin")
    public TokenResponse login(@RequestBody @Valid LoginRequest request) {
        return facade.login(request);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody @Valid RefreshRequest request) {
        return facade.refresh(request);
    }
}
