package com.marinboy.dao;

import org.apache.ibatis.annotations.Mapper;

// Oracle DB 연결이 정상인지 확인하기 위한 MyBatis DAO입니다.
@Mapper
public interface TestDao {

    // mapper XML의 readTime SQL과 연결되어 SELECT SYSDATE FROM DUAL 결과를 반환합니다.
    String readTime();
}
