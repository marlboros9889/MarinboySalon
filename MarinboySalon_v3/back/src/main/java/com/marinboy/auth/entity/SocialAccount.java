package com.marinboy.auth.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * 소셜 제공자 계정과 일반 회원 번호를 연결합니다.
 */
@Getter
@Setter
public class SocialAccount {

    private Long id;
    private Long userId;
    private String provider;
    private String providerUserId;
    private String providerEmail;
}
