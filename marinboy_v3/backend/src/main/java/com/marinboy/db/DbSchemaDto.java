package com.marinboy.db;

/** Oracle 테이블과 컬럼 구조 조회에 함께 사용하는 DTO입니다. */
public class DbSchemaDto {
    // Oracle에서 조회한 테이블 이름입니다.
    private String tableName;
    // 테이블에 포함된 컬럼 이름입니다.
    private String columnName;
    // VARCHAR2, NUMBER 등 Oracle 컬럼 자료형입니다.
    private String dataType;
    // NULL 값 허용 여부(Y/N)입니다.
    private String nullable;
    // 테이블 안에서 컬럼이 정의된 순서입니다.
    private Integer columnOrder;

    // MyBatis가 조회 결과를 DTO에 매핑할 때 사용하는 getter/setter입니다.
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public String getColumnName() { return columnName; }
    public void setColumnName(String columnName) { this.columnName = columnName; }
    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }
    public String getNullable() { return nullable; }
    public void setNullable(String nullable) { this.nullable = nullable; }
    public Integer getColumnOrder() { return columnOrder; }
    public void setColumnOrder(Integer columnOrder) { this.columnOrder = columnOrder; }
}
