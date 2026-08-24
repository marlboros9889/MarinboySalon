package com.marinboy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** 예약 커밋 이벤트를 선택적인 Google Calendar 연동으로 연결합니다. */
@Component
public class GoogleCalendarReservationListener {
    private static final Logger log = LoggerFactory.getLogger(GoogleCalendarReservationListener.class);
    private final ObjectProvider<GoogleCalendarService> googleCalendarService;

    public GoogleCalendarReservationListener(ObjectProvider<GoogleCalendarService> googleCalendarService) {
        this.googleCalendarService = googleCalendarService;
    }

    /** DB 커밋이 완료된 예약만 Calendar 도구에 전달해 외부 장애와 예약 저장을 분리합니다. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void synchronizeCalendar(GoogleCalendarReservationEvent reservationEvent) {
        GoogleCalendarService calendarService = googleCalendarService.getIfAvailable();
        if (calendarService == null) {
            log.info("Google Calendar 연동이 꺼져 있어 일정 등록을 건너뜁니다.");
            return;
        }
        calendarService.createReservationEvent(reservationEvent);
    }
}
