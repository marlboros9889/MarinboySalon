package com.marinboy.review.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.marinboy.auth.service.AuthUserJwtService;
import com.marinboy.review.dto.request.ReviewRequestDto;
import com.marinboy.review.dto.response.ReviewResponseDto;
import com.marinboy.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** 공개 후기 조회와 로그인 고객의 후기 등록 요청을 처리하는 REST API입니다. */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService service; private final AuthUserJwtService authUserJwtService;
    @GetMapping public ResponseEntity<List<ReviewResponseDto>> list() { return ResponseEntity.ok(service.getPublicList()); }
    @PostMapping public ResponseEntity<ReviewResponseDto> create(Authentication authentication, @Valid @RequestBody ReviewRequestDto request) {
        Long userId = authUserJwtService.getCurrentUserId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(userId, request));
    }
}
