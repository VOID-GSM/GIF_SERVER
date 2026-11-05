package com.example.gifserverv2.domain.push.service;

import nl.martijndwars.webpush.Urgency;

import java.time.Duration;

/**
 * 웹 푸시 전달 옵션.
 *
 * @param ttl     푸시 서비스(FCM 등)가 기기 오프라인 동안 메시지를 보관하는 최대 시간. 지나면 버려진다.
 * @param urgency 전달 우선순위 (null 이면 헤더를 보내지 않아 브라우저 기본값 normal)
 */
public record PushOptions(Duration ttl, Urgency urgency) {

    /** web-push 라이브러리 기본값과 같은 28일 */
    public static final PushOptions DEFAULT = new PushOptions(Duration.ofDays(28), null);

    public PushOptions {
        if (ttl == null || ttl.isNegative()) {
            throw new IllegalArgumentException("ttl 은 0 이상이어야 합니다.");
        }
    }

    public int ttlSeconds() {
        return Math.toIntExact(ttl.toSeconds());
    }
}
