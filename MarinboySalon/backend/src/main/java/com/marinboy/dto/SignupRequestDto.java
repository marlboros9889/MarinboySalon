package com.marinboy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** 회원가입 화면에서 서버가 검증해야 할 입력값만 전달합니다. */
public record SignupRequestDto(
        @NotBlank(message = "아이디를 입력하세요.") String username,
        @NotBlank(message = "비밀번호를 입력하세요.") String password,
        @NotBlank(message = "이름을 입력하세요.") String name,
        @NotBlank(message = "이메일을 입력하세요.") @Email(message = "이메일 형식을 확인하세요.") String email,
        @NotBlank(message = "연락처를 입력하세요.") String phone) {
}
