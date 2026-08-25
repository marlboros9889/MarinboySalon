package com.marinboy.serviceitem.dto.response;

import java.time.LocalDateTime;

import com.marinboy.serviceitem.entity.ServiceItem;

import lombok.Builder;
import lombok.Getter;

/**
 * 고객과 관리자 화면에 전달하는 시술 항목 응답입니다.
 */
@Getter
@Builder
public class ServiceItemResponseDto {

    private Long id;
    private String name;
    private Integer price;
    private Integer durationMinutes;
    private String description;
    private Boolean active;
    private LocalDateTime createdAt;

    public static ServiceItemResponseDto from(ServiceItem item) {
        return ServiceItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .price(item.getPrice())
                .durationMinutes(item.getDurationMinutes())
                .description(item.getDescription())
                .active(item.getActive())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
