package com.marinboy.serviceitem.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * 메뉴 이미지가 한 메뉴당 최대 4장으로 제한되는지 확인합니다.
 */
class ServiceItemRequestDtoValidationTest {

    @Test
    void imageUrlsAllowUpToFourItems() {
        ServiceItemRequestDto request = new ServiceItemRequestDto();
        request.setName("테스트 메뉴");
        request.setPrice(10000);
        request.setDurationMinutes(30);
        request.setImageUrls(List.of("/1.jpg", "/2.jpg", "/3.jpg", "/4.jpg", "/5.jpg"));

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<ServiceItemRequestDto>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("메뉴 이미지는 최대 4개까지 등록할 수 있습니다.");
        }
    }
}
