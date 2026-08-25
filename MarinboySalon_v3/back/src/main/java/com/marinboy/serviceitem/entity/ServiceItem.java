package com.marinboy.serviceitem.entity;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * service_item 테이블의 시술 항목 엔티티입니다.
 */
@Getter
@Setter
@NoArgsConstructor
public class ServiceItem {

    private Long id;
    private String name;
    private Integer price;
    private Integer durationMinutes;
    private String description;
    private Boolean active;
    private LocalDateTime createdAt;
}
