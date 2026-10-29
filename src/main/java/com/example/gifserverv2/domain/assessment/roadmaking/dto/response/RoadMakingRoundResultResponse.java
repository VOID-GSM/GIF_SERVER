package com.example.gifserverv2.domain.assessment.roadmaking.dto.response;

import com.example.gifserverv2.domain.assessment.roadmaking.entity.RoadMakingRound;

import java.time.Duration;

public record RoadMakingRoundResultResponse(
        Long roundId,
        int solvedCount,
        int wrongCount,
        double accuracy,
        long elapsedSeconds,
        int earnedCoin
) {
    public static RoadMakingRoundResultResponse from(RoadMakingRound round) {
        int attempts = round.getSolvedCount() + round.getWrongCount();
        double accuracy = attempts == 0 ? 0 : Math.round(round.getSolvedCount() * 1000.0 / attempts) / 10.0;
        long elapsed = Duration.between(round.getStartedAt(), round.getFinishedAt()).getSeconds();
        return new RoadMakingRoundResultResponse(round.getId(), round.getSolvedCount(), round.getWrongCount(), accuracy, elapsed,
                round.calculateCoin());
    }
}