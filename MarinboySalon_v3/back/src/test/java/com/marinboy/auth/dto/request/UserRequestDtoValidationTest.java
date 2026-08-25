package com.marinboy.auth.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * 회원가입 필수값이 Service 실행 전에 검증되는지 확인합니다.
 */
class UserRequestDtoValidationTest {

    @Test
    void passwordIsRequired() {
        UserRequestDto request = new UserRequestDto();
        request.setEmail("test@example.com");
        request.setName("검증 사용자");
        request.setPhone("010-0000-0000");

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("비밀번호는 필수입니다.");
        }
    }
}
