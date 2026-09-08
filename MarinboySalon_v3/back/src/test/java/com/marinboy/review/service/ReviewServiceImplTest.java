package com.marinboy.review.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.marinboy.reservation.repository.ReservationMapper;
import com.marinboy.review.repository.ReviewMapper;

/** 관리자 리뷰 삭제가 존재하지 않는 후기에는 성공한 것처럼 처리되지 않는지 확인합니다. */
class ReviewServiceImplTest {

    @Test
    void rejectsMissingReviewOnAdminDelete() {
        ReviewMapper reviewMapper = mock(ReviewMapper.class);
        when(reviewMapper.deleteById(99L)).thenReturn(0);
        ReviewServiceImpl service = new ReviewServiceImpl(reviewMapper, mock(ReservationMapper.class));

        assertThatThrownBy(() -> service.deleteByAdmin(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("삭제할 후기를 찾을 수 없습니다.");
    }
}
