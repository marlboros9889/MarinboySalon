package com.marinboy.review.service;

import java.util.List;
import com.marinboy.review.dto.request.ReviewRequestDto;
import com.marinboy.review.dto.response.ReviewResponseDto;

// 로그인 고객의 리뷰 등록과 공개 리뷰 목록 조회 기능을 정의합니다.
public interface ReviewService {
    ReviewResponseDto create(Long userId, ReviewRequestDto request);
    List<ReviewResponseDto> getPublicList();
}
