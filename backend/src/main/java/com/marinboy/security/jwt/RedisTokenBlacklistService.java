package com.marinboy.security.jwt;

import java.time.Duration;
import java.time.Instant;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/** 로그아웃된 JWT ID를 만료 시각까지 Redis에 보관합니다. */
@Service
public class RedisTokenBlacklistService {
    private static final String KEY_PREFIX = "security:jwt:blacklist:";
    private final StringRedisTemplate stringRedisTemplate;

    public RedisTokenBlacklistService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // 로그아웃 토큰은 원래 만료 시각까지만 저장해 Redis에 불필요한 키가 쌓이지 않게 합니다.
    public void blacklist(String tokenId, Instant expiresAt) {
        Duration remaining = Duration.between(Instant.now(), expiresAt);
        if (!remaining.isNegative() && !remaining.isZero()) {
            stringRedisTemplate.opsForValue().set(KEY_PREFIX + tokenId, "logout", remaining);
        }
    }

    // 필터가 JWT ID를 조회해 로그아웃 이후 재사용되는 요청을 차단합니다.
    public boolean isBlacklisted(String tokenId) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(KEY_PREFIX + tokenId));
    }
}
