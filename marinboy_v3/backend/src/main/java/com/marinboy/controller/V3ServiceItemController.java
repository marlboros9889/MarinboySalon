package com.marinboy.controller;

import java.net.URI;
import java.util.List;

import com.marinboy.dto.v3.ServiceItemRequestDto;
import com.marinboy.dto.v3.ServiceItemResponseDto;
import com.marinboy.service.ServiceItemV3Service;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** React 클라이언트가 사용할 v3 시술 메뉴 REST API입니다. */
@Profile("v3")
@RestController
@RequestMapping("/api/v3/service-items")
public class V3ServiceItemController {
    private final ServiceItemV3Service serviceItemV3Service;

    public V3ServiceItemController(ServiceItemV3Service serviceItemV3Service) {
        this.serviceItemV3Service = serviceItemV3Service;
    }

    @GetMapping
    public ResponseEntity<List<ServiceItemResponseDto>> findAll() {
        return ResponseEntity.ok(serviceItemV3Service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceItemResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceItemV3Service.findById(id));
    }

    @PostMapping
    public ResponseEntity<ServiceItemResponseDto> create(@Valid @RequestBody ServiceItemRequestDto request) {
        ServiceItemResponseDto response = serviceItemV3Service.create(request);
        return ResponseEntity.created(URI.create("/api/v3/service-items/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceItemResponseDto> update(@PathVariable Long id,
            @Valid @RequestBody ServiceItemRequestDto request) {
        return ResponseEntity.ok(serviceItemV3Service.update(id, request));
    }
}
