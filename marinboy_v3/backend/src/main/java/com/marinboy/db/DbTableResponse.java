package com.marinboy.db;

// Oracle에 생성된 MB_ 테이블명을 화면/API로 전달하기 위한 응답 DTO입니다.
public class DbTableResponse {

    // 현재 접속 계정이 소유한 테이블명입니다.
    private String tableName;

    // MyBatis가 결과 객체를 만들 수 있도록 기본 생성자를 제공합니다.
    public DbTableResponse() {
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
