package com.marinboy.security;

// JWT 권한 처리에서 공통으로 사용할 역할 값을 모아두는 클래스입니다.
public final class SecurityConstants {
    // 관리자 권한을 의미하는 문자열입니다.
    public static final String ROLE_ADMIN = "ADMIN";

    // 고객 권한을 의미하는 문자열입니다.
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    private SecurityConstants() {
        // 상수 전용 클래스이므로 외부에서 객체를 만들지 못하게 막습니다.
    }
}
