package com.marinboy.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Flutter 앱이 소셜 로그인 완료 뒤 제출하는 일회용 코드입니다. */
public record MobileOAuthExchangeRequest(
        @NotBlank(message = "소셜 로그인 코드가 필요합니다.") String code) {
}
