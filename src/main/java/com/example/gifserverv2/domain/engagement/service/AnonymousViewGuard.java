package com.example.gifserverv2.domain.engagement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.regex.Pattern;

/** 비로그인 조회를 "셀지 말지" Redis로 판정한다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnonymousViewGuard {

    private static final Duration DEDUP_TTL = Duration.ofMinutes(30);   // 같은 방문자 중복 방지 시간
    private static final Duration RATE_WINDOW = Duration.ofMinutes(1);
    private static final long RATE_LIMIT_PER_IP = 30;                   // IP당 분당 허용 횟수
    private static final Pattern VISITOR_ID = Pattern.compile("^[A-Za-z0-9_-]{16,64}$");

    // INCR과 EXPIRE를 한 번에 실행해서, 중간에 끊겨 만료 없는 키가 남는 일을 막는다.
    private static final DefaultRedisScript<Long> RATE_SCRIPT = new DefaultRedisScript<>("""
            local c = redis.call('INCR', KEYS[1])
            if c == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end
            return c
            """, Long.class);

    private final StringRedisTemplate redis;

    /** true면 이번 조회를 센다. */
    public boolean shouldCount(Long projectId, String visitorId, String ip, String userAgent) {
        try {
            if (isRateLimited(ip)) {
                return false;
            }
            String key = "view:" + projectId + ":" + viewerKey(visitorId, ip, userAgent);
            // 키가 없을 때만 저장 성공 -> 처음 온 방문자만 true (원자적이라 동시 요청도 1번만 통과)
            return Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(key, "1", DEDUP_TTL));
        } catch (DataAccessException e) {
            // Redis 장애 시 조회수 부풀림을 막기 위해 세지 않는다. API 자체는 정상 응답한다.
            log.warn("조회수 Redis 처리 실패, 이번 조회는 집계하지 않음: {}", e.getMessage());
            return false;
        }
    }

    private boolean isRateLimited(String ip) {
        Long count = redis.execute(RATE_SCRIPT, List.of("view-rate:" + ip),
                String.valueOf(RATE_WINDOW.toSeconds()));
        return count != null && count > RATE_LIMIT_PER_IP;
    }

    // 형식이 올바른 visitor id가 있으면 사용하고, 없거나 이상하면 IP + User-Agent 해시로 대체한다.
    private String viewerKey(String visitorId, String ip, String userAgent) {
        if (visitorId != null && VISITOR_ID.matcher(visitorId).matches()) {
            return "v:" + visitorId;
        }
        return "h:" + sha256(ip + "|" + (userAgent == null ? "" : userAgent));
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}