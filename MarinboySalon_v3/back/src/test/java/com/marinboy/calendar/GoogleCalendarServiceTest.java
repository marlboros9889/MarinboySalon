package com.marinboy.calendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import com.google.api.services.calendar.model.Event;
import com.marinboy.reservation.repository.ReservationMapper;

/** 실제 외부 호출 없이 캘린더 일정 규칙과 커밋 후 연결을 확인합니다. */
class GoogleCalendarServiceTest {

    @Test
    void createsReservationEventWithSeoulTimeAndImmediatePopup() {
        GoogleCalendarService calendarService = new GoogleCalendarService(
                "calendar-id", "C:/key.json", mock(ReservationMapper.class));
        GoogleCalendarReservationEvent reservation = new GoogleCalendarReservationEvent(
                1L,
                "캘린더 고객",
                "010-1234-5678",
                "웨이브 펌",
                LocalDateTime.of(2030, 1, 21, 10, 0),
                120);

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
        GoogleCalendarService calendarService = new GoogleCalendarService(
                "", "", mock(ReservationMapper.class));

        assertThatThrownBy(calendarService::validateConfiguration)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("GOOGLE_CALENDAR_ID");
    }

    @Test
    @SuppressWarnings("unchecked")
    void listenerConnectsCommittedReservationEventToCalendarTool() {
        ObjectProvider<GoogleCalendarService> provider = mock(ObjectProvider.class);
        GoogleCalendarService calendarService = mock(GoogleCalendarService.class);
        when(provider.getIfAvailable()).thenReturn(calendarService);
        GoogleCalendarReservationListener listener = new GoogleCalendarReservationListener(provider);
        GoogleCalendarReservationEvent reservation = new GoogleCalendarReservationEvent(
                1L, "캘린더 고객", "010-1234-5678", "웨이브 펌",
                LocalDateTime.of(2030, 1, 21, 10, 0), 120);

        listener.synchronizeCalendar(reservation);

        verify(calendarService).createReservationEvent(reservation);
    }
}
