package com.uber.springserver.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtUtil {

    private SecretKey keyFrom(String secret) {
        String effective = secret == null ? "" : secret;
        if (effective.length() < 32) {
            effective = (effective + "0123456789abcdef0123456789abcdef").substring(0, 32);
        }
        return Keys.hmacShaKeyFor(effective.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String id) {
        String secret = EnvUtil.get("JWT_ACCESS_SECRET", "default-access-secret-123456789012");
        return Jwts.builder()
                .claim("_id", id)
                .expiration(Date.from(Instant.now().plus(15, ChronoUnit.MINUTES)))
                .signWith(keyFrom(secret))
                .compact();
    }

    public String generateRefreshToken(String id) {
        String secret = EnvUtil.get("JWT_REFRESH_SECRET", "default-refresh-secret-12345678901");
        return Jwts.builder()
                .claim("_id", id)
                .expiration(Date.from(Instant.now().plus(7, ChronoUnit.DAYS)))
                .signWith(keyFrom(secret))
                .compact();
    }

    public Claims parseAccessToken(String token) {
        String secret = EnvUtil.get("JWT_ACCESS_SECRET", "default-access-secret-123456789012");
        return Jwts.parser()
                .verifyWith(keyFrom(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Claims parseRefreshToken(String token) {
        String secret = EnvUtil.get("JWT_REFRESH_SECRET", "default-refresh-secret-12345678901");
        return Jwts.parser()
                .verifyWith(keyFrom(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
