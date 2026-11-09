package com.example.gifserverv2.domain.ai.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 회고록 AI 도움받기의 사용자별 하루 호출 횟수 제한. (세 기능 합산, 한국 시간 자정에 초기화)
 * 서버 메모리에 두므로 재시작하면 초기화되고, 서버를 여러 대로 늘리면 서버마다 따로 센다.
 */
@Component
public class AiUsageLimiter {

    public static final int DAILY_LIMIT = 20;
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private final Clock clock;
    private final ConcurrentHashMap<Long, Usage> usages = new ConcurrentHashMap<>();

    private record Usage(LocalDate date, int count) {}

    public AiUsageLimiter() {
        this(Clock.system(ZONE));
    }

    AiUsageLimiter(Clock clock) {
        this.clock = clock;
    }

    /** 한 번 사용한 것으로 센다. 오늘 한도를 다 썼으면 429. */
    public void acquire(Long userId) {
        LocalDate today = LocalDate.now(clock);
        boolean[] acquired = {false};
        usages.compute(userId, (id, usage) -> {
            int count = (usage == null || !usage.date().equals(today)) ? 0 : usage.count();
            if (count >= DAILY_LIMIT) {
                return usage;
            }
            acquired[0] = true;
            return new Usage(today, count + 1);
        });
        if (!acquired[0]) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "AI 도움받기는 하루 " + DAILY_LIMIT + "회까지 사용할 수 있습니다. 내일 다시 시도해주세요.");
        }
    }

    /** AI 호출이 실패해 결과를 주지 못했으면 사용 횟수를 돌려준다. */
    public void release(Long userId) {
        LocalDate today = LocalDate.now(clock);
        usages.computeIfPresent(userId, (id, usage) ->
                usage.date().equals(today) && usage.count() > 0 ? new Usage(today, usage.count() - 1) : usage);
    }
}
