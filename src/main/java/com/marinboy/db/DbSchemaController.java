package com.marinboy.db;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

// 브라우저에서 DB 테이블과 컬럼 구조를 바로 확인할 수 있게 해주는 REST 컨트롤러입니다.
@RestController
@RequestMapping("/api/db")
@Tag(name = "DB 구조 점검", description = "관리자 전용 프로젝트 테이블·컬럼 조회 기능")
public class DbSchemaController {

    private final DbSchemaService dbSchemaService;

    // DB 구조 조회 서비스와 연결해 컨트롤러가 직접 SQL을 알지 않아도 되게 분리합니다.
    public DbSchemaController(DbSchemaService dbSchemaService) {
        this.dbSchemaService = dbSchemaService;
    }

    // http://localhost:8081/api/db/tables 로 접속하면 MB_ 테이블 목록을 확인할 수 있습니다.
    @GetMapping("/tables")
    @Operation(summary = "프로젝트 테이블 목록 조회")
    public List<DbSchemaDto> tables() {
        return dbSchemaService.getProjectTables();
    }

    // http://localhost:8081/api/db/columns 로 접속하면 MB_ 테이블의 컬럼 구조를 확인할 수 있습니다.
    @GetMapping("/columns")
    @Operation(summary = "프로젝트 테이블 컬럼 목록 조회")
    public List<DbSchemaDto> columns() {
        return dbSchemaService.getProjectColumns();
    }
}
