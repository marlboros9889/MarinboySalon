package com.marinboy.auth.dto;

// 현재 로그인한 사용자 정보를 화면 자동 입력에 사용하기 위한 응답 DTO입니다.
public record SessionUserResponse(String displayName, String email, String phone, String role) {
}
