package com.marinboy.dto;

/** DB 연결 검증 API가 데이터베이스 종류와 현재 시각을 반환할 때 사용하는 DTO입니다. */
public record DatabaseTimeResponse(
        String vendor,       // 연결된 데이터베이스 제품명
        String databaseTime) { // Oracle 서버 기준 현재 시각
}
