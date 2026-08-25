package com.marinboy.auth.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * 인증 객체에서 현재 회원 번호와 권한을 안전하게 꺼냅니다.
 */
@Service
public class AuthUserJwtService {

    public Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("로그인이 필요합니다.");
        }
        return Long.valueOf(authentication.getName());
    }

    public boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
