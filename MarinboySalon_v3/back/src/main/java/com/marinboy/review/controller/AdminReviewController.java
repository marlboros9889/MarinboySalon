package com.marinboy.review.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marinboy.review.dto.response.ReviewResponseDto;
import com.marinboy.review.service.ReviewService;

import lombok.RequiredArgsConstructor;

/** /api/admin 보안 규칙을 통해 관리자만 후기 전체 목록과 삭제를 관리합니다. */
@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService service;

    @GetMapping
    public ResponseEntity<List<ReviewResponseDto>> list() {
        return ResponseEntity.ok(service.getAdminList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteByAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
