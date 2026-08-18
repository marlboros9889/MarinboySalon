package com.marinboy.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.marinboy.dto.UserDto;
import com.marinboy.mapper.AuthMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Firebase ID 토큰을 검증하고 기존 MB_USER 관리자와 연결합니다. */
@Service
public class FirebaseIdentityService {
    private final ObjectProvider<FirebaseAuth> firebaseAuthProvider;
    private final AuthMapper authMapper;

    public FirebaseIdentityService(ObjectProvider<FirebaseAuth> firebaseAuthProvider, AuthMapper authMapper) {
        this.firebaseAuthProvider = firebaseAuthProvider;
        this.authMapper = authMapper;
    }

    public UserDto requireAdmin(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw unauthorized("Firebase 로그인 토큰이 필요합니다.");
        }
        FirebaseAuth firebaseAuth = firebaseAuthProvider.getIfAvailable();
        if (firebaseAuth == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Firebase 서버 설정이 필요합니다.");
        }

        try {
            FirebaseToken token = firebaseAuth.verifyIdToken(authorization.substring(7).trim());
            if (token.getEmail() == null || !token.isEmailVerified()) {
                throw unauthorized("이메일 인증이 완료된 Firebase 계정이 필요합니다.");
            }
            UserDto user = authMapper.findByEmail(token.getEmail());
            if (user == null || !SecurityConstants.ROLE_ADMIN.equals(user.getRole())) {
                throw unauthorized("등록된 관리자 계정이 아닙니다.");
            }
            return user;
        } catch (FirebaseAuthException | IllegalArgumentException exception) {
            throw unauthorized("유효하지 않거나 만료된 Firebase 로그인입니다.");
        }
    }

    private ResponseStatusException unauthorized(String message) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
    }
}
