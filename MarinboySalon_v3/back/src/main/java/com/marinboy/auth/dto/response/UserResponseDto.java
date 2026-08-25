package com.marinboy.auth.dto.response;

import java.time.LocalDateTime;

import com.marinboy.user.entity.AppUser;

import lombok.Builder;
import lombok.Getter;

/**
 * 비밀번호를 제외하고 프론트엔드에 전달하는 회원 응답 DTO입니다.
 */
@Getter
@Builder
public class UserResponseDto {

    private Long id;
    private String email;
    private String name;
    private String phone;
    private String role;
    private LocalDateTime createdAt;

    public static UserResponseDto from(AppUser user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
