package com.marinboy.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.marinboy.dto.GoogleCalendarDisplayDto;
import org.junit.jupiter.api.Test;

/** 관리자 캘린더 주소가 소스의 공개 환경변수 없이 생성되는지 검증합니다. */
class GoogleCalendarDisplayServiceTest {
    @Test
    void createsAdminCalendarEmbedUrlFromBackendConfiguration() {
        // 설정된 캘린더 ID는 URL 인코딩되어 관리자용 임베드 주소에만 포함되어야 합니다.
        GoogleCalendarDisplayService service = new GoogleCalendarDisplayService(true, "salon@example.com");

        GoogleCalendarDisplayDto result = service.getDisplayConfiguration();

        assertThat(result.configured()).isTrue();
        assertThat(result.embedUrl()).contains("calendar.google.com/calendar/embed")
                .contains("salon%40example.com")
                .contains("Asia%2FSeoul");
    }

    @Test
    void reportsDisabledCalendarWithoutExposingAnAddress() {
        // 연동을 끈 환경에서는 주소를 노출하지 않고 미설정 상태를 반환해야 합니다.
        GoogleCalendarDisplayService service = new GoogleCalendarDisplayService(false, "salon@example.com");

        GoogleCalendarDisplayDto result = service.getDisplayConfiguration();

        assertThat(result.configured()).isFalse();
        assertThat(result.embedUrl()).isEmpty();
    }
}
