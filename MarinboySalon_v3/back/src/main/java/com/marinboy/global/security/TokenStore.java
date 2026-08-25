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

    private static final String KEY_PREFIX = "marinboy:refresh:";

    private final StringRedisTemplate redisTemplate;

    public void saveRefreshToken(String userId, String token, long expiresInSeconds) {
        redisTemplate.opsForValue().set(
                KEY_PREFIX + userId,
                token,
                Duration.ofSeconds(expiresInSeconds));
    }

    public String getRefreshToken(String userId) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + userId);
    }

    public void deleteRefreshToken(String userId) {
        redisTemplate.delete(KEY_PREFIX + userId);
    }
}
