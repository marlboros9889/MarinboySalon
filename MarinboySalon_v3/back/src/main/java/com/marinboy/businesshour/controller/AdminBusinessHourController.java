package com.marinboy.businesshour.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marinboy.businesshour.dto.request.BusinessHourRequest;
import com.marinboy.businesshour.entity.BusinessHour;
import com.marinboy.businesshour.service.BusinessHourService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 전용 영업시간 REST API입니다.
 */
@RestController
@RequestMapping("/api/admin/business-hours")
@RequiredArgsConstructor
public class AdminBusinessHourController {

    private final BusinessHourService service;

    @GetMapping
    public ResponseEntity<List<BusinessHour>> list() {
        return ResponseEntity.ok(service.getList());
    }

    @PutMapping
    public ResponseEntity<Void> update(@Valid @RequestBody BusinessHourRequest request) {
        service.update(request);
        return ResponseEntity.noContent().build();
    }
}
