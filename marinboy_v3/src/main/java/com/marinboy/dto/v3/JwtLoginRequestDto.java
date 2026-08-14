package com.marinboy.dto.v3;

import jakarta.validation.constraints.NotBlank;

/** v3 JWT 로그인 요청 DTO입니다. */
public record JwtLoginRequestDto(@NotBlank String username, @NotBlank String password) { }
