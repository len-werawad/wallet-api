package com.lbk.socialbanking.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, String issuer, long accessTokenExpiration, long refreshTokenExpiration) {
}
