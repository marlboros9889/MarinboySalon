package com.marinboy.security.jwt;

import java.time.Duration;
import java.time.Instant;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/** 로그아웃된 JWT ID를 만료 시각까지 Redis에 보관합니다. */
@Profile("v3")
@Service
public class RedisTokenBlacklistService {
    private static final String KEY_PREFIX = "security:jwt:blacklist:";
    private final StringRedisTemplate stringRedisTemplate;

    public RedisTokenBlacklistService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void blacklist(String tokenId, Instant expiresAt) {
        Duration remaining = Duration.between(Instant.now(), expiresAt);
        if (!remaining.isNegative() && !remaining.isZero()) {
            stringRedisTemplate.opsForValue().set(KEY_PREFIX + tokenId, "logout", remaining);
        }
    }

    public boolean isBlacklisted(String tokenId) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(KEY_PREFIX + tokenId));
    }
}
