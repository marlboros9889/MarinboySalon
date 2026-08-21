package com.marinboy.service;

import com.marinboy.dto.UserDto;
import com.marinboy.security.SecurityConstants;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/** JWT 필터가 만든 인증 주체를 컨트롤러에서 안전하게 꺼내는 공통 서비스입니다. */
@Service
public class AuthenticatedUserService {

    public UserDto requireUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDto user)) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        return user;
    }

    public UserDto requireAdmin(Authentication authentication) {
        UserDto user = requireUser(authentication);
        if (!SecurityConstants.ROLE_ADMIN.equals(user.getRole())) {
            throw new IllegalArgumentException("관리자 로그인이 필요합니다.");
        }
        return user;
    }
}
