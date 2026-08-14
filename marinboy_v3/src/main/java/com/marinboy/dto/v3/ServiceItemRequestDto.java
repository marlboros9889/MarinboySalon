package com.marinboy.dto.v3;

import java.math.BigDecimal;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** v3 시술 메뉴 생성·수정 요청 전용 DTO입니다. */
public record ServiceItemRequestDto(
        @NotBlank String name,
        @NotBlank String category,
        @NotNull @Min(10) Integer durationMinutes,
        @NotNull @Min(1000) BigDecimal price,
        @NotBlank String description,
        Integer topRank
) { }
