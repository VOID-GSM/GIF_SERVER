package com.example.gifserverv2.domain.assessment.roadmaking.dto.response;

import com.example.gifserverv2.domain.assessment.roadmaking.entity.RoadMakingRound;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Duration;
import java.time.LocalDateTime;

@Schema(description = "길 만들기 라운드 시작 결과")
public record StartRoadMakingRoundResponse(
        @Schema(description = "라운드 ID", example = "11")
        Long roundId,
        @Schema(description = "전체 문항 수", example = "10")
        int questionCount,
        @Schema(description = "시작 일시", example = "2026-11-09T10:00:00")
        LocalDateTime startedAt,
        @Schema(description = "제한 시간 종료 일시 (시작 + 5분)", example = "2026-11-09T10:05:00")
        LocalDateTime expiresAt,
        @Schema(description = "남은 시간 (초)", example = "300")
        long remainingSeconds,
        @Schema(description = "현재 문항")
        RoadMakingProblemResponse problem
) {
    public static StartRoadMakingRoundResponse of(RoadMakingRound round, RoadMakingProblemResponse problem, LocalDateTime now) {
        long remaining = Math.max(0, Duration.between(now, round.getExpiresAt()).getSeconds());
        return new StartRoadMakingRoundResponse(round.getId(), round.getQuestionCount(), round.getStartedAt(),
                round.getExpiresAt(), remaining, problem);
    }
}
