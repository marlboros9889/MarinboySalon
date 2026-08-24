package com.marinboy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** 고객 정보 수정 API가 이름·이메일·연락처 외의 권한 필드를 받지 않게 제한합니다. */
public record UserProfileRequestDto(
        @NotBlank(message = "이름을 입력하세요.") String name,
        @NotBlank(message = "이메일을 입력하세요.") @Email(message = "이메일 형식을 확인하세요.") String email,
        @NotBlank(message = "연락처를 입력하세요.") String phone) {
}
