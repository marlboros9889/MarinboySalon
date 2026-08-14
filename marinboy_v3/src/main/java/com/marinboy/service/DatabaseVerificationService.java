package com.marinboy.service;

import com.marinboy.dao.TestDao;
import com.marinboy.dto.DatabaseTimeResponse;
import org.springframework.stereotype.Service;

// MyBatis DAO를 통해 실제 Oracle DB 연결 상태를 검증하는 서비스입니다.
@Service
public class DatabaseVerificationService {

    private final TestDao testDao;

    public DatabaseVerificationService(TestDao testDao) {
        // 생성자 주입으로 TestDao를 받아 테스트와 유지보수가 쉬운 구조로 만듭니다.
        this.testDao = testDao;
    }

    public DatabaseTimeResponse getDatabaseTime() {
        // mapper XML의 SELECT SYSDATE FROM DUAL 결과를 API 응답 형태로 변환합니다.
        return new DatabaseTimeResponse("ORACLE", testDao.readTime());
    }
}
