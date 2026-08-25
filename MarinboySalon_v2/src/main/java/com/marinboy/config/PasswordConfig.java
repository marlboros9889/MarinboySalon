package com.marinboy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 회원 비밀번호 암호화 도구를 한 곳에서 생성합니다.
 */
@Configuration
public class PasswordConfig {

    /**
     * 같은 비밀번호도 매번 다른 암호문으로 저장되는 BCrypt 방식을 사용합니다.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
