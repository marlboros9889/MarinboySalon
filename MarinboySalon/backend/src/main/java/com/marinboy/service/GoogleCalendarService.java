package com.marinboy.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/** 예약 DB 저장이 끝난 뒤에만 서비스 계정으로 원장님 캘린더에 일정을 등록합니다. */
@Service
@ConditionalOnProperty(name = "app.google-calendar.enabled", havingValue = "true")
public class GoogleCalendarService {
    private static final Logger log = LoggerFactory.getLogger(GoogleCalendarService.class);
    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final String calendarId;
    private final String credentialsPath;

    public GoogleCalendarService(
            @Value("${app.google-calendar.calendar-id:}") String calendarId,
            @Value("${app.google-calendar.credentials-path:}") String credentialsPath) {
        this.calendarId = calendarId;
        this.credentialsPath = credentialsPath;
    }

    /** 연동을 켠 경우에는 서버 시작 시 필수 설정과 키 파일을 먼저 확인합니다. */
    @PostConstruct
    void validateConfiguration() {
        if (calendarId == null || calendarId.isBlank()) {
            throw new IllegalStateException("GOOGLE_CALENDAR_ID가 비어 있습니다.");
        }
        if (credentialsPath == null || credentialsPath.isBlank()) {
            throw new IllegalStateException("GOOGLE_CALENDAR_CREDENTIALS_PATH가 비어 있습니다.");
        }

        Path keyFilePath = Path.of(credentialsPath).normalize();
        if (!keyFilePath.isAbsolute()) {
            throw new IllegalStateException("Google Calendar 서비스 계정 키는 절대 경로여야 합니다.");
        }
        if (!Files.isRegularFile(keyFilePath)) {
            throw new IllegalStateException("Google Calendar 서비스 계정 키 파일을 찾을 수 없습니다: " + keyFilePath);
        }
    }

    // Calendar 이벤트 연결 도구가 DB 커밋을 확인한 뒤 호출하므로 외부 장애가 예약 저장을 되돌리지 않습니다.
    @Async
    public void createReservationEvent(GoogleCalendarReservationEvent reservationEvent) {
        try {
            log.info("Google Calendar 예약 일정 등록을 시작합니다: {}", reservationEvent.reservationDateTime());
            Calendar calendar = createCalendarClient();
            Event event = createEvent(reservationEvent);
            Event createdEvent = calendar.events().insert(calendarId, event).execute();
            log.info("Google Calendar 예약 일정 등록을 완료했습니다: {}", createdEvent.getId());
        } catch (Exception exception) {
            // 캘린더 연동 실패는 재시도 대상이므로 예약 자체를 실패시키지 않고 서버 로그에만 남깁니다.
            log.warn("Google Calendar 예약 일정 등록에 실패했습니다: {}", exception.getMessage());
        }
    }

    private Calendar createCalendarClient() throws Exception {
        GoogleCredentials credentials;
        try (var keyFileStream = Files.newInputStream(Path.of(credentialsPath))) {
            credentials = GoogleCredentials.fromStream(keyFileStream)
                    .createScoped(CalendarScopes.CALENDAR);
        }

        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        return new Calendar.Builder(transport, GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                .setApplicationName("marinboySalon")
                .build();
    }

    Event createEvent(GoogleCalendarReservationEvent reservationEvent) {
        LocalDateTime startDateTime = reservationEvent.reservationDateTime();
        LocalDateTime endDateTime = startDateTime.plusMinutes(reservationEvent.durationMinutes());

        // 고객 연락처는 원장님의 예약 확인용 설명에만 저장하고 공개 응답에는 포함하지 않습니다.
        return new Event()
                .setSummary("[예약] " + reservationEvent.customerName() + "님 - " + reservationEvent.serviceName())
                .setDescription("고객명: " + reservationEvent.customerName()
                        + "\n연락처: " + reservationEvent.customerPhone()
                        + "\n시술: " + reservationEvent.serviceName())
                .setStart(toEventDateTime(startDateTime))
                .setEnd(toEventDateTime(endDateTime))
                // 캘린더 기본 알림과 별개로 일정 시작 시점에 Google Calendar 팝업을 요청합니다.
                .setReminders(new Event.Reminders()
                        .setUseDefault(false)
                        .setOverrides(List.of(new EventReminder().setMethod("popup").setMinutes(0))))
                .setColorId("2");
    }

    private EventDateTime toEventDateTime(LocalDateTime reservationDateTime) {
        long epochMillis = reservationDateTime.atZone(SEOUL_ZONE).toInstant().toEpochMilli();
        return new EventDateTime()
                .setDateTime(new DateTime(epochMillis))
                .setTimeZone(SEOUL_ZONE.getId());
    }
}
