package com.marinboy.global.security;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Refresh Token을 Redis에 저장하고 삭제합니다.
 */
@Component
@RequiredArgsConstructor
public class TokenStore {

    private static final String REFRESH_KEY_PREFIX = "marinboy:refresh:";
    private static final String BLOCKED_ACCESS_KEY_PREFIX = "marinboy:blocked-access:";

    private final StringRedisTemplate redisTemplate;

    public void saveRefreshToken(String userId, String token, long expiresInSeconds) {
        redisTemplate.opsForValue().set(
                REFRESH_KEY_PREFIX + userId,
                token,
                Duration.ofSeconds(expiresInSeconds));
    }

    public String getRefreshToken(String userId) {
        return redisTemplate.opsForValue().get(REFRESH_KEY_PREFIX + userId);
    }

    public void deleteRefreshToken(String userId) {
        redisTemplate.delete(REFRESH_KEY_PREFIX + userId);
    }

    /** 로그아웃한 Access Token을 실제 남은 시간만큼만 차단합니다. */
    public void blockAccessToken(String tokenId, long remainingSeconds) {
        if (tokenId == null || tokenId.isBlank() || remainingSeconds <= 0) {
            return;
        }
        redisTemplate.opsForValue().set(
                BLOCKED_ACCESS_KEY_PREFIX + tokenId,
                "logout",
                Duration.ofSeconds(remainingSeconds));
    }

    public boolean isAccessTokenBlocked(String tokenId) {
        if (tokenId == null || tokenId.isBlank()) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLOCKED_ACCESS_KEY_PREFIX + tokenId));
    }
}
