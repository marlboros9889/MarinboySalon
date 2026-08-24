package com.marinboy.api;

// 화면과 서버가 주고받는 API 주소를 한 곳에서 관리하는 클래스입니다.
public final class ApiPaths {

    public static final String DB_TIME = "/api/db-time";
    public static final String SERVICES = "/api/services";
    public static final String SERVICE_AVAILABLE_SLOTS = "/api/services/{serviceId}/available-slots";
    public static final String RESERVATIONS = "/api/reservations";
    public static final String RESERVATION_STATUS = "/api/reservations/{reservationId}/status";
    public static final String CUSTOMER_HISTORY = "/api/customers/history";
    public static final String AUTH_ME = "/api/auth/me";
    public static final String AUTH_LOGOUT = "/api/auth/logout";

    private ApiPaths() {
        // 주소 상수만 제공하는 클래스이므로 객체 생성을 막습니다.
    }
}
