package com.marinboy.global.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.marinboy.global.security.JwtProperties;

/**
 * 로컬 기본 JWT secret이 운영 프로필에서 쓰이지 않도록 기동 시 검사합니다.
 */
@Component
public class JwtSecretValidator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(JwtSecretValidator.class);
    private static final String LOCAL_DEFAULT_FRAGMENT = "marinboy-salon-local-secret-key";

    private final JwtProperties jwtProperties;
    private final Environment environment;
    private final boolean allowWeakLocalSecret;

    public JwtSecretValidator(JwtProperties jwtProperties,
                              Environment environment,
                              @Value("${app.jwt.allow-weak-local-secret:true}") boolean allowWeakLocalSecret) {
        this.jwtProperties = jwtProperties;
        this.environment = environment;
        this.allowWeakLocalSecret = allowWeakLocalSecret;
    }

    @Override
    public void run(ApplicationArguments args) {
        String secret = jwtProperties.getSecret();
        if (secret == null || secret.isBlank() || secret.length() < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET 환경 변수에 32자 이상의 강한 시크릿을 설정해 주세요.");
        }

        boolean productionLike = isProductionLike();
        boolean weak = secret.contains(LOCAL_DEFAULT_FRAGMENT);

        if (weak && (productionLike || !allowWeakLocalSecret)) {
            throw new IllegalStateException(
                    "로컬 기본 JWT secret은 운영/스테이징에서 사용할 수 없습니다. JWT_SECRET을 교체하세요.");
        }

        if (weak) {
            log.warn("로컬 기본 JWT secret을 사용 중입니다. 운영 배포 전 JWT_SECRET을 반드시 교체하세요.");
        }
    }

    private boolean isProductionLike() {
        for (String profile : environment.getActiveProfiles()) {
            String p = profile.toLowerCase();
            if (p.contains("prod") || p.contains("stage") || p.contains("staging")) {
                return true;
            }
        }
        return false;
    }
}
