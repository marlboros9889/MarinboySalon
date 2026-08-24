package com.marinboy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.services.calendar.model.Event;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

// 실제 Google API를 호출하지 않고 일정 내용과 시작 시각 변환 규칙을 검증합니다.
class GoogleCalendarServiceTest {

    @Test
    void createsReservationEventWithSeoulTimeAndImmediatePopup() {
        // 예약 시작·종료 시각과 즉시 팝업 규칙이 서울 시간 기준으로 만들어지는지 확인합니다.
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
        // 연동을 켠 채 필수 설정이 빠지면 서버 시작 단계에서 원인을 바로 알려야 합니다.
        GoogleCalendarService calendarService = new GoogleCalendarService("", "");

        assertThatThrownBy(calendarService::validateConfiguration)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("GOOGLE_CALENDAR_ID");
    }

    @Test
    @SuppressWarnings("unchecked")
    void listenerConnectsCommittedReservationEventToCalendarTool() {
        // Listener가 준비된 Calendar 도구에 이벤트만 전달하는지 외부 API 호출 없이 확인합니다.
        ObjectProvider<GoogleCalendarService> provider = mock(ObjectProvider.class);
        GoogleCalendarService calendarService = mock(GoogleCalendarService.class);
        when(provider.getIfAvailable()).thenReturn(calendarService);
        GoogleCalendarReservationListener listener = new GoogleCalendarReservationListener(provider);
        GoogleCalendarReservationEvent reservation = new GoogleCalendarReservationEvent(
                "캘린더 고객", "010-1234-5678", "웨이브 펌",
                LocalDateTime.of(2030, 1, 21, 10, 0), 120);

        listener.synchronizeCalendar(reservation);

        verify(calendarService).createReservationEvent(reservation);
    }
}
