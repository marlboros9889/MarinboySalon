package com.marinboy.review.service;

import java.util.List;
import com.marinboy.review.dto.request.ReviewRequestDto;
import com.marinboy.review.dto.response.ReviewResponseDto;

public interface ReviewService {
    ReviewResponseDto create(Long userId, ReviewRequestDto request);
    List<ReviewResponseDto> getPublicList();
}
