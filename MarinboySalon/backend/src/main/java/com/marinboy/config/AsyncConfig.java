package com.marinboy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** Google Calendar 외부 호출이 예약 API 응답을 지연시키지 않도록 비동기를 활성화합니다. */
@Configuration
@EnableAsync
public class AsyncConfig {
}
