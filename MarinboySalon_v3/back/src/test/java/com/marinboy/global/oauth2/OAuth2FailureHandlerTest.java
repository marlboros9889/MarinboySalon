package com.marinboy.global.oauth2;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;

// OAuth 콜백 실패가 기본 오류 화면이 아닌 안전한 프런트 로그인 화면으로 이동하는지 검증합니다.
class OAuth2FailureHandlerTest {

    @Test
    void redirectsToLoginWithoutProviderErrorDetails() throws Exception {
        OAuth2FailureHandler handler = new OAuth2FailureHandler();
        ReflectionTestUtils.setField(handler, "frontUrl", "http://localhost:3000");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationFailure(request, response, new BadCredentialsException("provider error"));

        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:3000/auth/login?oauthError=failed");
    }
}
