package com.marinboy.service;

import com.marinboy.dao.AuthDao;
import com.marinboy.dto.UserDto;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/** 로그인 입력값을 검증하고 사용자 계정을 조회하는 인증 서비스입니다. */
@Service
public class AuthService {
    private final AuthDao authDao;
    private final PasswordEncoder passwordEncoder;
    // 사용자 조회 SQL을 담당하는 DAO를 주입받습니다.
    public AuthService(AuthDao authDao, PasswordEncoder passwordEncoder) {
        this.authDao = authDao;
        this.passwordEncoder = passwordEncoder;
    }
    // 필수 입력값과 계정 일치 여부를 확인한 뒤 세션에 저장할 사용자를 반환합니다.
    @Transactional
    public UserDto login(String username, String password) {
        // null 입력은 DB 조회 전에 차단합니다.
        if (username == null || password == null) throw new IllegalArgumentException("아이디와 비밀번호를 입력하세요.");
        UserDto user = authDao.findByUsername(username.trim());
        // 어떤 값이 틀렸는지 구분하지 않아 계정 정보 노출을 줄입니다.
        if (user == null || !passwordMatches(password, user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        if (!user.getPassword().startsWith("$2")) {
            authDao.updatePassword(user.getId(), passwordEncoder.encode(password));
        }
        user.setPassword(null);
        return user;
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (storedPassword == null) return false;
        return storedPassword.startsWith("$2")
                ? passwordEncoder.matches(rawPassword, storedPassword)
                : storedPassword.equals(rawPassword);
    }
}
