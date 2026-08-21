package com.marinboy.service;

import com.marinboy.dto.UserDto;
import com.marinboy.mapper.AuthMapper;
import com.marinboy.security.SecurityConstants;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/** JWT 필터가 만든 인증 주체를 컨트롤러에서 안전하게 꺼내는 공통 서비스입니다. */
@Service
public class AuthenticatedUserService {
    private final AuthMapper authMapper;

    public AuthenticatedUserService(AuthMapper authMapper) {
        this.authMapper = authMapper;
    }

    public UserDto requireUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDto tokenUser)) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        // 이름·연락처 수정 직후에도 오래된 JWT claim 대신 현재 DB 고객 정보를 사용합니다.
        UserDto currentUser = authMapper.findByUsername(tokenUser.getUsername());
        if (currentUser == null || currentUser.getId() == null || !currentUser.getId().equals(tokenUser.getId())) {
            throw new IllegalArgumentException("로그인 계정을 확인할 수 없습니다. 다시 로그인해 주세요.");
        }
        currentUser.setPassword(null);
        currentUser.setDisplayName(currentUser.getName());
        return currentUser;
    }

    public UserDto requireAdmin(Authentication authentication) {
        UserDto user = requireUser(authentication);
        if (!SecurityConstants.ROLE_ADMIN.equals(user.getRole())) {
            throw new IllegalArgumentException("관리자 로그인이 필요합니다.");
        }
        return user;
    }
}
