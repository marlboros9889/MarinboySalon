package com.marinboy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/** 환경 변수로 활성화된 경우에만 관리자에게 새 예약 메일을 발송합니다. */
@Service
@ConditionalOnProperty(name = "app.notification.mail-enabled", havingValue = "true")
public class NotificationMailService {
    private static final Logger log = LoggerFactory.getLogger(NotificationMailService.class);
    private final JavaMailSender mailSender;

    public NotificationMailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void send(String email, String message) {
        if (email == null || email.isBlank()) return;
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(email);
            mail.setSubject("[Marinboy] 새 예약 신청 알림");
            mail.setText(message + "\n\n관리자 페이지에서 확인해 주세요.");
            mailSender.send(mail);
        } catch (RuntimeException exception) {
            // 메일 장애가 DB 알림과 예약 처리에 영향을 주지 않도록 로그만 남깁니다.
            log.warn("관리자 예약 알림 메일 발송에 실패했습니다: {}", exception.getMessage());
        }
    }
}
