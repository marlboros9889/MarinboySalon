package com.marinboy.api;

// 성공/안내 메시지만 간단히 내려줄 때 사용하는 공통 API 응답입니다.
public record ApiResponse(String message) {

    public static ApiResponse ok(String message) {
        // 컨트롤러에서 성공 메시지를 같은 형태로 반환할 수 있게 도와줍니다.
        return new ApiResponse(message);
    }
}
