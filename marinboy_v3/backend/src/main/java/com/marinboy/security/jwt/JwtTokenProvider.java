package com.marinboy.security.jwt;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/** v3 JWT 발급과 검증을 담당합니다. */
@Profile("v3")
@Component
public class JwtTokenProvider {
    private final SecretKey secretKey;
    private final long validitySeconds;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String encodedSecret,
            @Value("${jwt.access-token-validity-seconds}") long validitySeconds
    ) {
        byte[] decodedSecret = Decoders.BASE64.decode(encodedSecret);
        if (decodedSecret.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET은 32바이트 이상이어야 합니다.");
        }
        this.secretKey = Keys.hmacShaKeyFor(decodedSecret);
        this.validitySeconds = validitySeconds;
    }

    //1. 로그인 성공 사용자에게 짧은 수명의 서명 토큰을 발급합니다.
    public String createAccessToken(String username, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(validitySeconds)))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }
}
