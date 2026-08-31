package com.marinboy.global.oauth2;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 제공자 인증 또는 콜백이 실패해도 Spring 기본 오류 화면 대신 로그인 화면으로 안전하게 되돌립니다.
 */
@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    @Value("${app.front-url}")
    private String frontUrl;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        // 제공자가 돌려준 상세 오류·인가 코드는 URL과 로그에 노출하지 않습니다.
        response.sendRedirect(frontUrl + "/auth/login?oauthError=failed");
    }
}
