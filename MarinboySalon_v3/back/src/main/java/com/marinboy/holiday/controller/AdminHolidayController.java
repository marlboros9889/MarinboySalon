package com.marinboy.holiday.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marinboy.holiday.dto.request.HolidayRequest;
import com.marinboy.holiday.entity.Holiday;
import com.marinboy.holiday.service.HolidayService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 전용 임시 휴무일 REST API입니다.
 */
@RestController
@RequestMapping("/api/admin/holidays")
@RequiredArgsConstructor
public class AdminHolidayController {

    private final HolidayService service;

    @GetMapping
    public ResponseEntity<List<Holiday>> list() {
        return ResponseEntity.ok(service.getList());
    }

    @PostMapping
    public ResponseEntity<Holiday> insert(@Valid @RequestBody HolidayRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.insert(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
