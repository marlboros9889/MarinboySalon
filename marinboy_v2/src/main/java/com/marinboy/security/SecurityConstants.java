package com.marinboy.security;

// 로그인과 권한 처리에서 공통으로 사용할 세션 키와 역할 값을 모아두는 클래스입니다.
public final class SecurityConstants {

    // 로그인 성공 후 세션에 저장할 사용자 정보의 키입니다.
    public static final String LOGIN_USER = "LOGIN_USER";
    public static final String LOGIN_PROVIDER = "LOGIN_PROVIDER";

    // 관리자 권한을 의미하는 문자열입니다.
    public static final String ROLE_ADMIN = "ADMIN";

    // 고객 권한을 의미하는 문자열입니다.
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    private SecurityConstants() {
        // 상수 전용 클래스이므로 외부에서 객체를 만들지 못하게 막습니다.
    }
}
