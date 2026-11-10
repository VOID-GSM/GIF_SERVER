package com.example.gifserverv2.domain.assessment.roadmaking.dto.response;

import com.example.gifserverv2.domain.assessment.roadmaking.entity.RoadMakingRound;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Duration;

@Schema(description = "길 만들기 라운드 결과")
public record RoadMakingRoundResultResponse(
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
        @Schema(description = "소요 시간 (초)", example = "212")
        long elapsedSeconds,
        @Schema(description = "끝까지 진행했는지. 이탈이면 false", example = "true")
        boolean completed,
        @Schema(description = "지급된 코인", example = "90")
        int earnedCoin
) {
    public static RoadMakingRoundResultResponse from(RoadMakingRound round) {
        int attempts = round.getAnsweredCount();
        double accuracy = attempts == 0 ? 0 : Math.round(round.getSolvedCount() * 1000.0 / attempts) / 10.0;
        long elapsed = Duration.between(round.getStartedAt(), round.getFinishedAt()).getSeconds();
        return new RoadMakingRoundResultResponse(round.getId(), round.getQuestionCount(), round.getSolvedCount(),
                round.getWrongCount(), accuracy, elapsed, round.isCompleted(), round.getEarnedCoin());
    }
}
