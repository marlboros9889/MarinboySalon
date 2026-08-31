package com.marinboy.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marinboy.auth.dto.request.LoginRequest;
import com.marinboy.auth.dto.request.UserRequestDto;
import com.marinboy.auth.dto.response.UserResponseDto;
import com.marinboy.user.entity.AppUser;
import com.marinboy.user.repository.AppUserMapper;

import lombok.RequiredArgsConstructor;

/** 회원가입, 로그인 확인, 회원 조회의 실제 처리 순서를 담당합니다. */
@Service
@RequiredArgsConstructor
@Transactional
public class AppUserServiceImpl implements AppUserService {

    private final AppUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto signup(UserRequestDto request) {
        if (userMapper.countByEmail(request.getEmail()) > 0) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        AppUser user = new AppUser();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setRole("CUSTOMER");
        userMapper.insert(user);

        return UserResponseDto.from(userMapper.selectById(user.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto login(LoginRequest request) {
        AppUser user = userMapper.selectByEmail(request.getEmail());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        return UserResponseDto.from(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto findById(Long id) {
        AppUser user = userMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다.");
        }
        return UserResponseDto.from(user);
    }

    @Override
    @Transactional(readOnly = true)
    public String findRoleByUserId(Long id) {
        return findById(id).getRole();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userMapper.countByEmail(email) > 0;
    }
}
