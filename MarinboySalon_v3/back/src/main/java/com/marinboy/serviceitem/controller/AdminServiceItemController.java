package com.marinboy.serviceitem.controller;

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

import com.marinboy.serviceitem.dto.request.ServiceItemRequestDto;
import com.marinboy.serviceitem.dto.response.ServiceItemResponseDto;
import com.marinboy.serviceitem.service.ServiceItemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 관리자가 시술 항목을 등록, 수정, 비활성화하는 REST API입니다.
 */
@RestController
@RequestMapping("/api/admin/service-items")
@RequiredArgsConstructor
public class AdminServiceItemController {

    private final ServiceItemService service;

    @GetMapping
    public ResponseEntity<List<ServiceItemResponseDto>> list() {
        return ResponseEntity.ok(service.getAdminList());
    }

    @PostMapping
    public ResponseEntity<ServiceItemResponseDto> insert(
            @Valid @RequestBody ServiceItemRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.insert(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceItemResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody ServiceItemRequestDto request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
