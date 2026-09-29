package com.marinboy.global.oauth2;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** 휴대폰 앱으로 JWT를 직접 노출하지 않도록 일회용 로그인 코드를 보관합니다. */
@Component
public class MobileOAuthCodeStore {

    private static final String KEY_PREFIX = "marinboy:mobile-oauth:";
    private final StringRedisTemplate redisTemplate;

    public MobileOAuthCodeStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String createCode(Long userId) {
        String code = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(KEY_PREFIX + code, userId.toString(), Duration.ofMinutes(1));
        return code;
    }

    /** 교환된 코드는 즉시 삭제하여 다시 사용할 수 없게 합니다. */
    public Long consumeUserId(String code) {
        String userId = redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + code);
        return userId == null ? null : Long.valueOf(userId);
    }
}
