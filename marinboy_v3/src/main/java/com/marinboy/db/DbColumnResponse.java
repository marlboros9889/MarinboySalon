package com.marinboy.db;

// Oracle 테이블의 컬럼 정보를 화면/API로 전달하기 위한 응답 DTO입니다.
public class DbColumnResponse {

    // 컬럼이 속한 테이블명입니다.
    private String tableName;

    // Oracle에 생성된 컬럼명입니다.
    private String columnName;

    // VARCHAR2, NUMBER, DATE 같은 Oracle 데이터 타입입니다.
    private String dataType;

    // NULL 허용 여부를 Y/N 값으로 전달합니다.
    private String nullable;

    // 테이블 안에서 컬럼이 생성된 순서입니다.
    private Integer columnOrder;

    // MyBatis가 결과 객체를 만들 수 있도록 기본 생성자를 제공합니다.
    public DbColumnResponse() {
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getNullable() {
        return nullable;
    }

    public void setNullable(String nullable) {
        this.nullable = nullable;
    }

    public Integer getColumnOrder() {
        return columnOrder;
    }

    public void setColumnOrder(Integer columnOrder) {
        this.columnOrder = columnOrder;
    }
}
