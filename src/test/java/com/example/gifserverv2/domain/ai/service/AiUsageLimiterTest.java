package com.example.gifserverv2.domain.ai.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiUsageLimiterTest {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private static class MutableClock extends Clock {
        private Instant now;

        MutableClock(String instant) {
            this.now = Instant.parse(instant);
        }

        void set(String instant) {
            this.now = Instant.parse(instant);
        }

        @Override
        public ZoneId getZone() {
            return SEOUL;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private static void useUpToday(AiUsageLimiter limiter, Long userId) {
        for (int i = 0; i < AiUsageLimiter.DAILY_LIMIT; i++) {
            limiter.acquire(userId);
        }
    }

    @Test
    void 하루_한도를_넘기면_429() {
        AiUsageLimiter limiter = new AiUsageLimiter(new MutableClock("2026-11-06T03:00:00Z"));
        useUpToday(limiter, 1L);

        assertThatThrownBy(() -> limiter.acquire(1L))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS));
    }

    @Test
    void 한도는_사용자마다_따로_센다() {
        AiUsageLimiter limiter = new AiUsageLimiter(new MutableClock("2026-11-06T03:00:00Z"));
        useUpToday(limiter, 1L);

        assertThatCode(() -> limiter.acquire(2L)).doesNotThrowAnyException();
    }

    @Test
    void 실패해서_돌려준_횟수는_다시_쓸_수_있다() {
        AiUsageLimiter limiter = new AiUsageLimiter(new MutableClock("2026-11-06T03:00:00Z"));
        useUpToday(limiter, 1L);
        limiter.release(1L);

        assertThatCode(() -> limiter.acquire(1L)).doesNotThrowAnyException();
    }

    @Test
    void 한국_시간_자정이_지나면_초기화된다() {
        MutableClock clock = new MutableClock("2026-11-06T14:59:00Z"); // 11/06 23:59 KST
        AiUsageLimiter limiter = new AiUsageLimiter(clock);
        useUpToday(limiter, 1L);

        clock.set("2026-11-06T15:00:00Z"); // 11/07 00:00 KST
        assertThatCode(() -> limiter.acquire(1L)).doesNotThrowAnyException();
    }
}
