package com.marinboy.dto;

/** 화면에는 인증·DB 내부 필드를 제외한 사용자 정보만 반환합니다. */
public record UserResponseDto(
        Long id,
        String username,
        String name,
        String displayName,
        String email,
        String phone,
        String role,
        String loginProvider,
        boolean profileComplete) {

    public static UserResponseDto from(UserDto user) {
        return new UserResponseDto(
                user.getId(), user.getUsername(), user.getName(), user.getDisplayName(),
                user.getEmail(), user.getPhone(), user.getRole(), user.getLoginProvider(),
                user.isProfileComplete());
    }
}
