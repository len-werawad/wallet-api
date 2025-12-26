package com.lbk.socialbanking.common.api;

import com.lbk.socialbanking.common.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtService {

    private final JwtProperties props;

    public JwtService(JwtProperties props) {
        this.props = props;
    }

    public String mintAccessToken(String userId) {
        return mint(userId, props.accessTokenExpiration(), Map.of("typ", "access"));
    }

    public String mintRefreshToken(String userId) {
        return mint(userId, props.refreshTokenExpiration(), Map.of("typ", "refresh"));
    }

    private String mint(String userId, long ttlSeconds, Map<String, Object> claims) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttlSeconds);

        return Jwts.builder()
                .issuer(props.issuer())
                .subject(userId)
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    public JwtParsed parseAndValidate(String token) {
        var payload = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new JwtParsed(payload.getSubject(), payload.get("typ", String.class));
    }

    public record JwtParsed(String userId, String typ) {
    }
}
