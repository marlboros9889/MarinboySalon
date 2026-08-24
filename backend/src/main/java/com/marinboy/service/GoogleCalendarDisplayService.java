package com.marinboy.service;

import com.marinboy.dto.GoogleCalendarDisplayDto;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** 백엔드 캘린더 ID로 관리자 전용 표시 주소를 만들어 프론트 설정 누락을 막습니다. */
@Service
public class GoogleCalendarDisplayService {
    private final boolean enabled;
    private final String calendarId;

    public GoogleCalendarDisplayService(
            @Value("${app.google-calendar.enabled:false}") boolean enabled,
            @Value("${app.google-calendar.calendar-id:}") String calendarId) {
        this.enabled = enabled;
        this.calendarId = calendarId;
    }

    // 관리자 화면에는 비밀 키를 보내지 않고 공개 가능한 임베드 주소만 만들어 반환합니다.
    public GoogleCalendarDisplayDto getDisplayConfiguration() {
        if (!enabled || calendarId == null || calendarId.isBlank()) {
            return new GoogleCalendarDisplayDto(false, "");
        }

        String encodedCalendarId = URLEncoder.encode(calendarId.trim(), StandardCharsets.UTF_8);
        String embedUrl = "https://calendar.google.com/calendar/embed?src="
                + encodedCalendarId + "&ctz=Asia%2FSeoul";
        return new GoogleCalendarDisplayDto(true, embedUrl);
    }
}
