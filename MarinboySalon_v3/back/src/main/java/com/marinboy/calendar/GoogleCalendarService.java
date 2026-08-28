package com.marinboy.calendar;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.AclRule;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.marinboy.reservation.repository.ReservationMapper;

import jakarta.annotation.PostConstruct;

/** 서비스 계정으로 원장님 Google Calendar에 예약 일정을 등록합니다. */
@Service
@ConditionalOnProperty(name = "app.google-calendar.enabled", havingValue = "true")
public class GoogleCalendarService {

    private static final Logger log = LoggerFactory.getLogger(GoogleCalendarService.class);
    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final String calendarId;
    private final String credentialsPath;
    private final ReservationMapper reservationMapper;

    public GoogleCalendarService(
            @Value("${app.google-calendar.calendar-id:}") String calendarId,
            @Value("${app.google-calendar.credentials-path:}") String credentialsPath,
            ReservationMapper reservationMapper) {
        this.calendarId = calendarId;
        this.credentialsPath = credentialsPath;
        this.reservationMapper = reservationMapper;
    }

    /** 연동을 켠 경우 서버 시작 시 필수 설정과 키 파일을 먼저 확인합니다. */
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

    /** 외부 호출은 비동기로 처리하고 성공한 이벤트 번호만 예약 DB에 연결합니다. */
    @Async
    @Transactional
    public void createReservationEvent(GoogleCalendarReservationEvent reservationEvent) {
        try {
            Calendar calendar = createCalendarClient();
            Event createdEvent = calendar.events().insert(calendarId, createEvent(reservationEvent)).execute();
            reservationMapper.updateCalendarEventId(reservationEvent.reservationId(), createdEvent.getId());
            log.info("Google Calendar 예약 일정 등록을 완료했습니다: reservationId={}",
                    reservationEvent.reservationId());
        } catch (Exception exception) {
            // 외부 장애가 이미 저장된 예약을 되돌리지 않도록 실패 내용을 로그에 남깁니다.
            log.warn("Google Calendar 예약 일정 등록에 실패했습니다: reservationId={}, message={}",
                    reservationEvent.reservationId(), exception.getMessage());
        }
    }

    /**
     * 관리자 화면에서 서비스 계정의 실제 캘린더 접근 권한을 확인합니다.
     * 키와 토큰은 응답에 포함하지 않아 외부에 노출되지 않습니다.
     */
    public Map<String, Object> checkConnection() {
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            Calendar calendar = createCalendarClient();
            CalendarList calendarList = calendar.calendarList().list().execute();
            List<CalendarListEntry> calendars = calendarList.getItems();
            int accessibleCalendarCount = calendars == null ? 0 : calendars.size();
            CalendarListEntry targetCalendar = findTargetCalendar(calendars);

            result.put("accessibleCalendarCount", accessibleCalendarCount);
            if (targetCalendar == null) {
                result.put("connected", false);
                result.put("message", "서비스 계정의 캘린더 목록에 대상 캘린더가 없습니다.");
                result.put("reason", "캘린더 공유가 서버 계정에 적용되지 않음");
                return result;
            }

            result.put("connected", true);
            result.put("calendarId", targetCalendar.getId());
            result.put("summary", targetCalendar.getSummary());
        } catch (Exception exception) {
            result.put("connected", false);
            result.put("message", "서비스 계정이 설정된 캘린더에 접근할 수 없습니다.");
            result.put("reason", extractReason(exception));
        }

        return result;
    }

    /**
     * 기존 개인 캘린더 공유가 막힌 경우 서비스 계정 전용 예약 캘린더를 만듭니다.
     * 생성한 캘린더는 요청한 관리자 계정에도 쓰기 권한으로 공유합니다.
     */
    public Map<String, String> createDedicatedCalendar(String ownerEmail) throws Exception {
        Calendar calendarClient = createCalendarClient();
        com.google.api.services.calendar.model.Calendar newCalendar =
                new com.google.api.services.calendar.model.Calendar()
                        .setSummary("Marinboy Salon 예약")
                        .setTimeZone(SEOUL_ZONE.getId());

        com.google.api.services.calendar.model.Calendar createdCalendar =
                calendarClient.calendars().insert(newCalendar).execute();

        try {
            AclRule shareRule = new AclRule()
                    .setRole("writer")
                    .setScope(new AclRule.Scope().setType("user").setValue(ownerEmail));
            calendarClient.acl().insert(createdCalendar.getId(), shareRule)
                    .setSendNotifications(true)
                    .execute();
        } catch (Exception exception) {
            // 공유에 실패한 빈 캘린더가 남지 않도록 생성 직후 정리합니다.
            calendarClient.calendars().delete(createdCalendar.getId()).execute();
            throw exception;
        }

        return Map.of(
                "calendarId", createdCalendar.getId(),
                "summary", createdCalendar.getSummary());
    }

    private CalendarListEntry findTargetCalendar(List<CalendarListEntry> calendars) {
        if (calendars == null) {
            return null;
        }

        for (CalendarListEntry calendar : calendars) {
            if (calendarId.equalsIgnoreCase(calendar.getId())) {
                return calendar;
            }
        }
        return null;
    }

    private String extractReason(Exception exception) {
        String message = exception.getMessage();
        if (message != null && message.contains("404")) {
            return "캘린더를 찾을 수 없음";
        }
        if (message != null && message.contains("403")) {
            return "캘린더 권한이 없음";
        }
        return "Google Calendar 연결 오류";
    }

    private Calendar createCalendarClient() throws Exception {
        GoogleCredentials credentials;
        try (var keyFileStream = Files.newInputStream(Path.of(credentialsPath))) {
            credentials = GoogleCredentials.fromStream(keyFileStream).createScoped(CalendarScopes.CALENDAR);
        }
        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        return new Calendar.Builder(
                transport,
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("marinboySalon")
                .build();
    }

    Event createEvent(GoogleCalendarReservationEvent reservationEvent) {
        LocalDateTime startDateTime = reservationEvent.reservationDateTime();
        LocalDateTime endDateTime = startDateTime.plusMinutes(reservationEvent.durationMinutes());
        return new Event()
                .setSummary("[예약] " + reservationEvent.customerName() + "님 - " + reservationEvent.serviceName())
                .setDescription("고객명: " + reservationEvent.customerName()
                        + "\n연락처: " + reservationEvent.customerPhone()
                        + "\n시술: " + reservationEvent.serviceName())
                .setStart(toEventDateTime(startDateTime))
                .setEnd(toEventDateTime(endDateTime))
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
