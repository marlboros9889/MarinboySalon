package com.marinboy.review.entity;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 완료 예약에 고객이 한 번만 남기는 후기 정보입니다. */
@Getter
@Setter
public class Review {
    private Long id;
    private Long reservationId;
    private Long userId;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
    private String userName;
    private String serviceName;
}
