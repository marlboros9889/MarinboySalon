package com.marinboy.global.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

/**
 * Access Token과 Refresh Token을 만들고 검증합니다.
 */
@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties properties;

    public String createAccessToken(String userId, Map<String, Object> claims) {
        return createToken(userId, claims, properties.getAccessTokenExpSeconds());
    }

    public String createRefreshToken(String userId) {
        return createToken(userId, Map.of("type", "refresh"), properties.getRefreshTokenExpSeconds());
    }

    public Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String createToken(String userId, Map<String, Object> claims, int expiresInSeconds) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + expiresInSeconds * 1000L);

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(userId)
                .addClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiresAt)
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
