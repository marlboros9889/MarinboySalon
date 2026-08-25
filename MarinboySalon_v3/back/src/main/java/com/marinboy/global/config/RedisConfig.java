package com.marinboy.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

/**
 * OAuth 인가 요청 객체를 Redis에 저장할 전용 템플릿입니다.
 */
@Configuration
public class RedisConfig {

    @Bean
    RedisTemplate<String, OAuth2AuthorizationRequest> oauthAuthorizationRedisTemplate(
            RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, OAuth2AuthorizationRequest> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new JdkSerializationRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
