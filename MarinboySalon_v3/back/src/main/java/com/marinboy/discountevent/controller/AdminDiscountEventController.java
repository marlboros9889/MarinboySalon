package com.marinboy.discountevent.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marinboy.discountevent.dto.request.DiscountEventRequestDto;
import com.marinboy.discountevent.dto.response.DiscountEventResponseDto;
import com.marinboy.discountevent.service.DiscountEventService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** 관리자만 기간 할인 이벤트를 관리하는 REST API입니다. */
@RestController
@RequestMapping("/api/admin/discount-events")
@RequiredArgsConstructor
public class AdminDiscountEventController {

    private final DiscountEventService service;

    @GetMapping
    public ResponseEntity<List<DiscountEventResponseDto>> list() {
        return ResponseEntity.ok(service.getAll());
    }

    @PostMapping
    public ResponseEntity<DiscountEventResponseDto> create(@Valid @RequestBody DiscountEventRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiscountEventResponseDto> update(
            @PathVariable Long id, @Valid @RequestBody DiscountEventRequestDto request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
