package com.marinboy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * 마린보이 살롱 프로젝트를 시작하는 메인 클래스입니다.
 */
@SpringBootApplication
public class MarinboySalonApplication extends SpringBootServletInitializer {

    /**
     * 외부 톰캣에서도 프로젝트를 실행할 수 있도록 시작 클래스를 등록합니다.
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MarinboySalonApplication.class);
    }

    /**
     * 내장 톰캣으로 프로젝트를 실행합니다.
     */
    public static void main(String[] args) {
        SpringApplication.run(MarinboySalonApplication.class, args);
    }
}
