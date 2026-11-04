package com.example.gifserverv2.domain.assessment.roadmaking.dto.response;

import com.example.gifserverv2.domain.assessment.roadmaking.entity.RoadMakingRound;

import java.time.Duration;

public record RoadMakingRoundResultResponse(
        Long roundId,
        int questionCount,
        int solvedCount,
        int wrongCount,
        double accuracy,
        long elapsedSeconds,
        boolean completed,
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
