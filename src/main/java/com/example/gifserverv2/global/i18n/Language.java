package com.example.gifserverv2.global.i18n;

// 응답 언어. 프론트는 lang 쿼리 파라미터로 보낸다.
public enum Language {
    KO, JA;

    // 값이 없거나 모르는 값이면 한국어로 응답한다. (잘못된 값으로 400이 나서 화면이 깨지지 않게)
    public static Language from(String value) {
        if (value != null && value.trim().equalsIgnoreCase("ja")) {
            return JA;
        }
        return KO;
    }
}
