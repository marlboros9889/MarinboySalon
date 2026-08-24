package com.marinboy.security.jwt;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.marinboy.dto.UserDto;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/** 로그인 사용자 정보를 담은 JWT의 발급과 서명 검증을 담당합니다. */
@Component
public class JwtTokenProvider {
    private final SecretKey secretKey;
    private final long validitySeconds;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String encodedSecret,
            @Value("${jwt.access-token-validity-seconds}") long validitySeconds
    ) {
        // 서버가 약한 키로 시작하지 않도록 Base64 해독 결과가 32바이트 이상인지 먼저 확인합니다.
        if (encodedSecret == null || encodedSecret.isBlank()) {
            throw new IllegalArgumentException("JWT_SECRET 환경변수를 설정해야 합니다.");
        }
        byte[] decodedSecret = Decoders.BASE64.decode(encodedSecret.trim());
        if (decodedSecret.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET은 32바이트 이상이어야 합니다.");
        }
        this.secretKey = Keys.hmacShaKeyFor(decodedSecret);
        this.validitySeconds = validitySeconds;
    }

    //1. 로그인 성공 사용자에게 짧은 수명의 서명 토큰을 발급합니다.
    public String createAccessToken(UserDto user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .claim("name", user.getName())
                .claim("email", user.getEmail())
                .claim("phone", user.getPhone())
                .claim("role", user.getRole())
                .claim("loginProvider", user.getLoginProvider())
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(validitySeconds)))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    //2. API 요청에서 받은 토큰의 서명과 만료를 검증하고 인증에 필요한 claim을 반환합니다.
    public Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }
}
