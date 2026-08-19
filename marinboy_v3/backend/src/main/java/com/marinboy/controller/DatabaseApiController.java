package com.marinboy.controller;

import com.marinboy.dto.DatabaseTimeResponse;
import com.marinboy.service.DatabaseVerificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

// 브라우저나 테스트에서 DB 연결 상태를 확인할 수 있는 API 컨트롤러입니다.
@RestController
@Tag(name = "시스템 점검", description = "데이터베이스 연결 상태 확인 기능")
public class DatabaseApiController {

    private final DatabaseVerificationService databaseVerificationService;

    public DatabaseApiController(DatabaseVerificationService databaseVerificationService) {
        // DB 검증 로직을 컨트롤러에 직접 쓰지 않고 서비스에 위임합니다.
        this.databaseVerificationService = databaseVerificationService;
    }

    @GetMapping("/api/db-time")
    @Operation(summary = "데이터베이스 현재 시간 조회")
    public DatabaseTimeResponse databaseTime() {
        // Oracle과 MyBatis mapper가 정상 연결되었는지 확인하는 응답을 반환합니다.
        return databaseVerificationService.getDatabaseTime();
    }
}
