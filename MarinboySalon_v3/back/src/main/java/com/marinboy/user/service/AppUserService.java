package com.marinboy.user.service;

import com.marinboy.auth.dto.request.LoginRequest;
import com.marinboy.auth.dto.request.UserRequestDto;
import com.marinboy.auth.dto.response.UserResponseDto;

/**
 * 회원 기능에서 Controller가 사용할 작업 목록입니다.
 */
public interface AppUserService {

    UserResponseDto signup(UserRequestDto request);

    UserResponseDto login(LoginRequest request);

    UserResponseDto findById(Long id);

    String findRoleByUserId(Long id);

    boolean existsByEmail(String email);
}
