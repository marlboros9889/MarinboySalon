package com.marinboy.discountevent.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.marinboy.discountevent.entity.DiscountEvent;

import lombok.Builder;
import lombok.Getter;

/** 관리자 화면과 예약 금액 계산에 전달하는 할인 이벤트 응답입니다. */
@Getter
@Builder
public class DiscountEventResponseDto {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal discountRate;

    public static DiscountEventResponseDto from(DiscountEvent event) {
        return DiscountEventResponseDto.builder()
                .id(event.getId())
                .name(event.getName())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .discountRate(event.getDiscountRate())
                .build();
    }
}
