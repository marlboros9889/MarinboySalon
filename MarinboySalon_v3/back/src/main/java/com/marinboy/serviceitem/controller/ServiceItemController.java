package com.marinboy.serviceitem.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marinboy.serviceitem.dto.response.ServiceItemResponseDto;
import com.marinboy.serviceitem.service.ServiceItemService;

import lombok.RequiredArgsConstructor;

/**
 * 고객이 시술 메뉴를 조회하는 공개 REST API입니다.
 */
@RestController
@RequestMapping("/api/service-items")
@RequiredArgsConstructor
public class ServiceItemController {

    private final ServiceItemService service;

    @GetMapping
    public ResponseEntity<List<ServiceItemResponseDto>> list() {
        return ResponseEntity.ok(service.getActiveList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceItemResponseDto> detail(@PathVariable Long id) {
        return ResponseEntity.ok(service.getDetail(id));
    }
}
