package com.marinboy.db;

import java.util.List;
import org.springframework.stereotype.Service;

// 컨트롤러가 DB 구조 조회를 요청했을 때 DAO를 호출하는 서비스 계층입니다.
@Service
public class DbSchemaService {

    private final DbSchemaDao dbSchemaDao;

    // MyBatis DAO를 주입받아 DB 메타데이터 조회에 사용합니다.
    public DbSchemaService(DbSchemaDao dbSchemaDao) {
        this.dbSchemaDao = dbSchemaDao;
    }

    // 포트폴리오 설명에 필요한 프로젝트 테이블 목록을 반환합니다.
    public List<DbSchemaDto> getProjectTables() {
        return dbSchemaDao.findProjectTables();
    }

    // 각 테이블에 어떤 컬럼이 들어있는지 확인할 수 있는 목록을 반환합니다.
    public List<DbSchemaDto> getProjectColumns() {
        return dbSchemaDao.findProjectColumns();
    }
}
