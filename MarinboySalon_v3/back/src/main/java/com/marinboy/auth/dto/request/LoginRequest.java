package com.marinboy.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 로그인 화면에서 JSON으로 전달하는 값입니다.
 */
@Getter
@Setter
public class LoginRequest {

    @Email(message = "이메일 형식을 확인해 주세요.")
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
