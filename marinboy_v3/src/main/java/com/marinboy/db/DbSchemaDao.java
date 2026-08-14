package com.marinboy.db;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;

// Oracle 데이터 사전(USER_TABLES, USER_TAB_COLUMNS)을 조회하는 MyBatis DAO입니다.
@Mapper
public interface DbSchemaDao {

    // 현재 계정에 생성된 우리 프로젝트 테이블 목록을 조회합니다.
    List<DbSchemaDto> findProjectTables();

    // 현재 계정에 생성된 우리 프로젝트 테이블들의 컬럼 구조를 조회합니다.
    List<DbSchemaDto> findProjectColumns();
}
