package com.marinboy.util;

// 고객 연락처를 관리자 화면이나 로그에서 안전하게 보여주기 위한 유틸리티입니다.
public final class PhoneMaskingUtil {

    private PhoneMaskingUtil() {
        // 유틸리티 클래스이므로 객체 생성을 막습니다.
    }

    public static String maskMiddle(String phone) {
        // 전화번호가 없거나 너무 짧으면 잘못된 문자열 처리를 피하기 위해 빈 문자열을 반환합니다.
        if (phone == null || phone.isBlank()) {
            return "";
        }

        // 010-2222-1111 형태는 가운데 번호만 가려 개인정보 노출을 줄입니다.
        String[] parts = phone.split("-");
        if (parts.length == 3) {
            return parts[0] + "-****-" + parts[2];
        }

        // 하이픈이 없는 번호는 뒤 4자리만 남겨 같은 고객 여부를 대략 확인할 수 있게 합니다.
        if (phone.length() > 4) {
            return "****" + phone.substring(phone.length() - 4);
        }

        return "****";
    }
}
