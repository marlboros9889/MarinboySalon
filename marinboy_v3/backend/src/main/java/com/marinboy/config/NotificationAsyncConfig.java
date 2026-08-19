package com.marinboy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** 이메일 발송이 예약 및 알림 API 응답을 지연시키지 않도록 비동기를 활성화합니다. */
@Configuration
@EnableAsync
public class NotificationAsyncConfig {
}
