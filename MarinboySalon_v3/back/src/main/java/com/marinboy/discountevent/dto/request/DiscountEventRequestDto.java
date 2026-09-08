package com.marinboy.discountevent.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** 할인 이벤트 등록과 수정 화면에서 전달받는 값입니다. */
@Getter
@Setter
public class DiscountEventRequestDto {

    @NotBlank(message = "이벤트 이름을 입력해 주세요.")
    @Size(max = 100, message = "이벤트 이름은 100자 이하로 입력해 주세요.")
    private String name;

    @NotNull(message = "시작일을 선택해 주세요.")
    private LocalDate startDate;

    @NotNull(message = "종료일을 선택해 주세요.")
    private LocalDate endDate;

    @NotNull(message = "할인율을 입력해 주세요.")
    @DecimalMin(value = "0.01", message = "할인율은 0보다 커야 합니다.")
    @DecimalMax(value = "100.00", message = "할인율은 100 이하여야 합니다.")
    private BigDecimal discountRate;
}
