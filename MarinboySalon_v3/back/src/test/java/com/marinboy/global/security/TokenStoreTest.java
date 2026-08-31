package com.marinboy.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/** 로그아웃 Access Token이 남은 시간만 Redis에서 차단되는지 확인합니다. */
@ExtendWith(MockitoExtension.class)
class TokenStoreTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private TokenStore tokenStore;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        tokenStore = new TokenStore(redisTemplate);
    }

    @Test
    void blocksAccessTokenForRemainingLifetime() {
        tokenStore.blockAccessToken("token-jti", 321L);

        verify(valueOperations).set(
                "marinboy:blocked-access:token-jti",
                "logout",
                Duration.ofSeconds(321L));
    }

    @Test
    void readsAccessTokenBlacklist() {
        when(redisTemplate.hasKey("marinboy:blocked-access:token-jti")).thenReturn(true);

        assertThat(tokenStore.isAccessTokenBlocked("token-jti")).isTrue();
    }
}
