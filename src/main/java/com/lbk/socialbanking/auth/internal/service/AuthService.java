package com.lbk.socialbanking.auth.internal.service;

import com.lbk.socialbanking.auth.web.LoginRequest;
import com.lbk.socialbanking.auth.web.RefreshRequest;
import com.lbk.socialbanking.auth.web.TokenResponse;

public interface AuthService {
    TokenResponse login(LoginRequest request);

    TokenResponse refresh(RefreshRequest request);
}
