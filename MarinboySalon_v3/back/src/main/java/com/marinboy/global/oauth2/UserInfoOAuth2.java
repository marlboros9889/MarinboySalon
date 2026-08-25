package com.marinboy.global.oauth2;

/**
 * Google, Kakao, Naver의 서로 다른 응답 이름을 같은 형태로 바꿉니다.
 */
public interface UserInfoOAuth2 {

    String getProviderUserId();

    String getEmail();

    String getName();
}
