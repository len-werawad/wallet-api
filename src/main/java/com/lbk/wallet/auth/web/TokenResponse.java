package com.lbk.wallet.auth.web;

public record TokenResponse(String accessToken, String refreshToken, long expiresInSeconds, String tokenType) {
}
