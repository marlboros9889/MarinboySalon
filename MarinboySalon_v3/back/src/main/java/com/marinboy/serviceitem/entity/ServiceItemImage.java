package com.marinboy.serviceitem.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * service_item_image 테이블의 메뉴 이미지 엔티티입니다.
 */
@Getter
@Setter
@NoArgsConstructor
public class ServiceItemImage {

    private Long id;
    private Long serviceItemId;
    private String imageUrl;
    private Integer displayOrder;
}
