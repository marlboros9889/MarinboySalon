package com.marinboy.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * REST 인증 실패와 권한 실패가 서로 다른 상태 코드와 JSON을 반환하는지 확인합니다.
 */
class RestSecurityHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void unauthenticatedRequestReturns401Json() throws Exception {
        RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint(objectMapper);
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(
                new MockHttpServletRequest(),
                response,
                new InsufficientAuthenticationException("로그인이 필요합니다."));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("로그인이 필요합니다.");
    }

    @Test
    void unauthorizedRoleReturns403Json() throws Exception {
        RestAccessDeniedHandler handler = new RestAccessDeniedHandler(objectMapper);
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(
                new MockHttpServletRequest(),
                response,
                new AccessDeniedException("권한이 없습니다."));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("접근 권한이 없습니다.");
    }
}
