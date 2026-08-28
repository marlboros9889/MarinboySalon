package com.marinboy.review.repository;

import java.util.List;
import com.marinboy.review.entity.Review;

/** review Mapper XML과 연결되는 후기 SQL 인터페이스입니다. */
public interface ReviewMapper {
    int insert(Review review);
    int countByReservationId(Long reservationId);
    List<Review> selectPublicList();
    List<Review> selectMyList(Long userId);
}
