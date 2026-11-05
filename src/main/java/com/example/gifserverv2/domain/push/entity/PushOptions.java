package com.example.gifserverv2.domain.push.entity;

import java.time.Duration;
import java.util.regex.Pattern;

/**
 * 웹 푸시 전달 옵션.
 *
 * @param ttl     푸시 서비스(FCM 등)가 기기 오프라인 동안 메시지를 보관하는 최대 시간. 지나면 버려진다.
 * @param urgency 전달 우선순위 (null 이면 헤더를 보내지 않아 브라우저 기본값 normal)
 * @param topic   같은 topic 의 메시지가 푸시 서비스에 보관 중이면 새 메시지로 교체된다. (null 이면 교체 없음)
 */
public record PushOptions(Duration ttl, Urgency urgency, String topic) {

    /** RFC 8030: 32자 이하, URL-safe base64 문자만 허용 */
    private static final Pattern TOPIC_PATTERN = Pattern.compile("[A-Za-z0-9_-]{1,32}");

    /** web-push 라이브러리 기본값과 같은 28일 */
    public static final PushOptions DEFAULT = new PushOptions(Duration.ofDays(28), null);

    public PushOptions {
        if (ttl == null || ttl.isNegative()) {
            throw new IllegalArgumentException("ttl 은 0 이상이어야 합니다.");
        }
        if (topic != null && !TOPIC_PATTERN.matcher(topic).matches()) {
            throw new IllegalArgumentException("topic 은 32자 이하의 URL-safe base64 문자여야 합니다: " + topic);
        }
    }

    public PushOptions(Duration ttl, Urgency urgency) {
        this(ttl, urgency, null);
    }

    public PushOptions withTopic(String topic) {
        return new PushOptions(ttl, urgency, topic);
    }

    /**
     * TTL 이 max 를 넘지 않게 줄인 옵션을 반환한다. (max 가 음수면 0)
     */
    public PushOptions withTtlAtMost(Duration max) {
        Duration capped = max.isNegative() ? Duration.ZERO : max;
        return ttl.compareTo(capped) <= 0 ? this : new PushOptions(capped, urgency, topic);
    }

    public int ttlSeconds() {
        return Math.toIntExact(ttl.toSeconds());
    }

    /** Web Push Urgency 헤더 값 (RFC 8030) */
    public enum Urgency {
        VERY_LOW, LOW, NORMAL, HIGH
    }
}
