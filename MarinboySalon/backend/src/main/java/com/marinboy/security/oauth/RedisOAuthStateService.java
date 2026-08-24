package com.marinboy.security.oauth;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/** 소셜 로그인 위조 방지 state를 서버 메모리가 아닌 Redis에 잠시 보관합니다. */
@Service
public class RedisOAuthStateService {
    private static final String KEY_PREFIX = "security:oauth:state:";
    private static final Duration STATE_TTL = Duration.ofMinutes(5);
    private final SecureRandom secureRandom = new SecureRandom();
    private final StringRedisTemplate stringRedisTemplate;

    public RedisOAuthStateService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // 소셜 로그인 시작 시 예측하기 어려운 state를 만들고 제공자 이름과 함께 5분간 저장합니다.
    public String create(String provider) {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        stringRedisTemplate.opsForValue().set(KEY_PREFIX + state, provider, STATE_TTL);
        return state;
    }

    // 콜백에서는 state를 한 번만 꺼내 제공자까지 비교하여 위조와 재사용을 함께 막습니다.
    public boolean consume(String state, String provider) {
        if (state == null || state.isBlank()) return false;
        String savedProvider = stringRedisTemplate.opsForValue().getAndDelete(KEY_PREFIX + state);
        return provider.equals(savedProvider);
    }
}
