package com.marinboy.dto;

/** 관리자 화면에 필요한 캘린더 표시 가능 여부와 보안이 적용된 주소만 전달합니다. */
public record GoogleCalendarDisplayDto(boolean configured, String embedUrl) {
}
