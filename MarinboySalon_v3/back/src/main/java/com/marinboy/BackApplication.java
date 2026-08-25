package com.marinboy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 마린보이 살롱 v3 백엔드 REST API 실행 클래스입니다.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@MapperScan("com.marinboy.**.repository")
public class BackApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackApplication.class, args);
    }
}
