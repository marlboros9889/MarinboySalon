package com.marinboy.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// 날짜와 시간을 화면 표시용 문자열로 바꾸는 공통 유틸리티입니다.
public final class DateTimeUtil {

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private DateTimeUtil() {
        // 유틸리티 클래스이므로 객체 생성을 막습니다.
    }

    public static String toDisplayText(LocalDateTime dateTime) {
        // 예약 일시가 없을 때 화면에 null이 그대로 보이지 않도록 빈 문자열을 반환합니다.
        if (dateTime == null) {
            return "";
        }

        // 고객과 관리자가 읽기 쉬운 날짜/시간 형식으로 변환합니다.
        return dateTime.format(DISPLAY_FORMATTER);
    }
}
