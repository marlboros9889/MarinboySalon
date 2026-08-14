package com.marinboy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// MyBatis 방식으로 재설계한 마린보이 애플리케이션의 시작점입니다.
@SpringBootApplication
public class MarinboyMybatisApplication {

    public static void main(String[] args) {
        // Spring Boot 내장 톰캣을 실행하고 컨트롤러, 서비스, DAO 빈을 등록합니다.
        SpringApplication.run(MarinboyMybatisApplication.class, args);
    }
}
