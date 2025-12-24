package com.lbk.wallet.auth.internal.service;

import com.lbk.wallet.auth.web.LoginRequest;
import com.lbk.wallet.auth.web.RefreshRequest;
import com.lbk.wallet.auth.web.TokenResponse;

public interface AuthService {
    TokenResponse login(LoginRequest request);

    TokenResponse refresh(RefreshRequest request);
}
