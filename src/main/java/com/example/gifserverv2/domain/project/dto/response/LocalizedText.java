package com.example.gifserverv2.domain.project.dto.response;

// 요청 언어로 고른 글. translated 가 false 면 한국어 원문이다.
public record LocalizedText(String text, boolean translated) {

    public static LocalizedText original(String text) {
        return new LocalizedText(text, false);
    }

    public static LocalizedText translated(String text) {
        return new LocalizedText(text, true);
    }
}
