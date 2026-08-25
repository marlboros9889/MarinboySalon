package com.marinboy.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 회원가입과 내 정보 수정에 사용하는 요청 DTO입니다.
 */
@Getter
@Setter
public class UserRequestDto {

    @Email(message = "이메일 형식을 확인해 주세요.")
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 6, message = "비밀번호는 6자 이상 입력해 주세요.")
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "연락처는 필수입니다.")
    private String phone;
}
