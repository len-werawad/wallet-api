package com.lbk.socialbanking.auth.web;

public record TokenResponse(String accessToken, String refreshToken, long expiresInSeconds, String tokenType) {
}
