package com.marinboy.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** 고객이 완료된 예약에 남기는 후기 입력값입니다. */
@Getter
@Setter
public class ReviewRequestDto {
    @NotNull(message = "예약 번호가 필요합니다.") private Long reservationId;
    @NotNull(message = "별점을 선택해 주세요.") @Min(1) @Max(5) private Integer rating;
    @NotBlank(message = "후기 내용을 입력해 주세요.") @Size(max = 500, message = "후기는 500자 이하여야 합니다.") private String content;
}
