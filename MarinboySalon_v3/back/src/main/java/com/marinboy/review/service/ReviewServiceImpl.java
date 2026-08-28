package com.marinboy.review.service;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.marinboy.reservation.entity.Reservation;
import com.marinboy.reservation.repository.ReservationMapper;
import com.marinboy.review.dto.request.ReviewRequestDto;
import com.marinboy.review.dto.response.ReviewResponseDto;
import com.marinboy.review.entity.Review;
import com.marinboy.review.repository.ReviewMapper;
import lombok.RequiredArgsConstructor;

/** 완료 예약의 소유자만 후기를 한 번 작성하도록 처리합니다. */
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {
    private final ReviewMapper reviewMapper;
    private final ReservationMapper reservationMapper;

    @Override
    public ReviewResponseDto create(Long userId, ReviewRequestDto request) {
        Reservation reservation = reservationMapper.selectById(request.getReservationId());
        if (reservation == null || !reservation.getUserId().equals(userId)) throw new SecurityException("본인의 예약에만 후기를 작성할 수 있습니다.");
        if (!"COMPLETED".equals(reservation.getStatus())) throw new IllegalArgumentException("시술 완료 후에만 후기를 작성할 수 있습니다.");
        if (reviewMapper.countByReservationId(request.getReservationId()) > 0) throw new IllegalArgumentException("이 예약에는 이미 후기가 등록되어 있습니다.");
        Review review = new Review();
        review.setReservationId(request.getReservationId());
        review.setUserId(userId);
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        reviewMapper.insert(review);
        review.setCreatedAt(LocalDateTime.now());
        review.setUserName(reservation.getUserName());
        review.setServiceName(reservation.getServiceName());
        return ReviewResponseDto.from(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getPublicList() {
        List<ReviewResponseDto> responses = new ArrayList<>();
        for (Review review : reviewMapper.selectPublicList()) responses.add(ReviewResponseDto.from(review));
        return responses;
    }
}
