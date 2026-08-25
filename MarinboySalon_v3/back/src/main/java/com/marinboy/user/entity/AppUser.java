package com.marinboy.user.entity;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * user_account 테이블 한 행을 담는 회원 엔티티입니다.
 */
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

    private Long id;
    private String email;
    private String password;
    private String name;
    private String phone;
    private String role;
    private LocalDateTime createdAt;
}
