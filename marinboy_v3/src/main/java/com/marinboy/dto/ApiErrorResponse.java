package com.marinboy.dto;

/** API 오류를 화면에서 읽을 수 있는 메시지로 반환하는 응답 DTO입니다. */
public record ApiErrorResponse(String message) {
}
