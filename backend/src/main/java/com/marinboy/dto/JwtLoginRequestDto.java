package com.marinboy.dto;

import jakarta.validation.constraints.NotBlank;

/** JWT 로그인에 필요한 아이디와 비밀번호를 받습니다. */
public record JwtLoginRequestDto(@NotBlank String username, @NotBlank String password) { }
