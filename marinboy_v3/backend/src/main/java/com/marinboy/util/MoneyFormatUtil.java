package com.marinboy.util;

import java.text.NumberFormat;
import java.util.Locale;

// 시술 금액을 한국 원화 표기처럼 읽기 쉽게 바꾸는 공통 유틸리티입니다.
public final class MoneyFormatUtil {

    private MoneyFormatUtil() {
        // 유틸리티 클래스이므로 객체 생성을 막습니다.
    }

    public static String toWonText(Integer amount) {
        // 금액이 없을 때 화면에 null이 노출되지 않도록 빈 문자열을 반환합니다.
        if (amount == null) {
            return "";
        }

        // 65000 같은 숫자를 65,000원 형태로 변환합니다.
        return NumberFormat.getNumberInstance(Locale.KOREA).format(amount) + "원";
    }
}
