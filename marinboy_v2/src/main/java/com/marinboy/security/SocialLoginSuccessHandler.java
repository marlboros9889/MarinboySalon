package com.marinboy.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import com.marinboy.dto.UserDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

// 소셜 로그인 성공 시 사용자 정보를 세션에 담고 메인 화면으로 이동시키는 처리 클래스입니다.
@Component
public class SocialLoginSuccessHandler implements AuthenticationSuccessHandler {

    // OAuth2 인증 결과에서 제공자와 사용자 정보를 추출한 뒤 세션 기반 로그인 상태를 만듭니다.
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            // OAuth2AuthenticationToken의 principal은 이미 OAuth2User 타입이므로 바로 사용자 속성을 꺼냅니다.
            OAuth2User oauth2User = oauthToken.getPrincipal();
            SocialLoginUser socialUser = SocialLoginUser.from(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauth2User.getAttributes()
            );

            UserDto loginUser = new UserDto();
            loginUser.setUsername(socialUser.provider().name().toLowerCase() + "_" + socialUser.socialId());
            loginUser.setName(socialUser.name() == null || socialUser.name().isBlank()
                    ? "카카오 사용자" : socialUser.name());
            loginUser.setEmail(socialUser.email());
            loginUser.setRole(socialUser.role());
            loginUser.setLoginProvider(oauthToken.getAuthorizedClientRegistrationId().toUpperCase());
            request.getSession(true).setAttribute(SecurityConstants.LOGIN_USER, loginUser);
            request.getSession(true).setAttribute(SecurityConstants.LOGIN_PROVIDER,
                    oauthToken.getAuthorizedClientRegistrationId().toUpperCase());
        }

        response.sendRedirect("/");
    }
}
