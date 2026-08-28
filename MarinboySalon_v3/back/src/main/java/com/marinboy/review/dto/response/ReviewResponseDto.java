package com.marinboy.review.dto.response;

import java.time.LocalDateTime;
import com.marinboy.review.entity.Review;
import lombok.Builder;
import lombok.Getter;

/** 고객 화면에 필요한 후기 결과만 전달합니다. */
@Getter
@Builder
public class ReviewResponseDto {
    private Long id; private Long reservationId; private Integer rating; private String content;
    private LocalDateTime createdAt; private String userName; private String serviceName;
    public static ReviewResponseDto from(Review review) {
        return ReviewResponseDto.builder().id(review.getId()).reservationId(review.getReservationId())
                .rating(review.getRating()).content(review.getContent()).createdAt(review.getCreatedAt())
                .userName(review.getUserName()).serviceName(review.getServiceName()).build();
    }
}
