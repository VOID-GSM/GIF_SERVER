package com.example.gifserverv2.domain.assessment.roadmaking.dto.response;

import com.example.gifserverv2.domain.assessment.roadmaking.entity.RoadMakingRound;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "길 만들기 라운드 요약")
public record RoadMakingRoundSummaryResponse(
        @Schema(description = "라운드 ID", example = "11")
        Long roundId,
        @Schema(description = "전체 문항 수", example = "10")
        int questionCount,
        @Schema(description = "맞힌 문항 수", example = "7")
        int solvedCount,
        @Schema(description = "틀린 문항 수", example = "3")
        int wrongCount,
        @Schema(description = "정답률 (%, 소수 첫째 자리)", example = "70.0")
        double accuracy,
        @Schema(description = "소요 시간 (초). 진행 중이면 null", example = "212", nullable = true)
        Long elapsedSeconds,
        @Schema(description = "종료 여부 (완료·이탈 포함). false면 진행 중", example = "true")
        boolean finished,
        @Schema(description = "끝까지 진행했는지. 이탈이면 false", example = "true")
        boolean completed,
        @Schema(description = "지급된 코인", example = "90")
        int earnedCoin,
        @Schema(description = "시작 일시", example = "2026-11-09T10:00:00")
        LocalDateTime startedAt
) {
    public static RoadMakingRoundSummaryResponse from(RoadMakingRound round) {
        return new RoadMakingRoundSummaryResponse(
                round.getId(),
                round.getQuestionCount(),
                round.getSolvedCount(),
                round.getWrongCount(),
                round.getAccuracy(),
                round.getElapsedSeconds(),
                !round.isInProgress(),
                round.isCompleted(),
                round.getEarnedCoin(),
                round.getStartedAt()
        );
    }
}
