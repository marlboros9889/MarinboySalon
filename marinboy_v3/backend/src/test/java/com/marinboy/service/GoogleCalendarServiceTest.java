package com.marinboy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.api.services.calendar.model.Event;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

// 실제 Google API를 호출하지 않고 일정 내용과 시작 시각 변환 규칙을 검증합니다.
class GoogleCalendarServiceTest {

    @Test
    void createsReservationEventWithSeoulTimeAndImmediatePopup() {
        GoogleCalendarService calendarService = new GoogleCalendarService("calendar-id", "C:/key.json");
        GoogleCalendarReservationEvent reservation = new GoogleCalendarReservationEvent(
                "캘린더 고객",
                "010-1234-5678",
                "웨이브 펌",
                LocalDateTime.of(2030, 1, 21, 10, 0),
                120
        );

        Event event = calendarService.createEvent(reservation);

        assertThat(event.getSummary()).isEqualTo("[예약] 캘린더 고객님 - 웨이브 펌");
        assertThat(event.getStart().getTimeZone()).isEqualTo("Asia/Seoul");
        assertThat(event.getEnd().getDateTime().getValue() - event.getStart().getDateTime().getValue())
                .isEqualTo(120L * 60L * 1000L);
        assertThat(event.getReminders().getOverrides()).singleElement()
                .satisfies(reminder -> {
                    assertThat(reminder.getMethod()).isEqualTo("popup");
                    assertThat(reminder.getMinutes()).isZero();
                });
    }

    @Test
    void rejectsMissingCalendarConfigurationBeforeServerStarts() {
        GoogleCalendarService calendarService = new GoogleCalendarService("", "");

        assertThatThrownBy(calendarService::validateConfiguration)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("GOOGLE_CALENDAR_ID");
    }
}
