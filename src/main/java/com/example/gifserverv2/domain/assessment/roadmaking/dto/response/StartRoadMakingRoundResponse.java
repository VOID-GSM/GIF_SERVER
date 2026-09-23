package com.example.gifserverv2.domain.assessment.roadmaking.dto.response;

import com.example.gifserverv2.domain.assessment.roadmaking.entity.RoadMakingRound;

import java.time.Duration;
import java.time.LocalDateTime;

public record StartRoadMakingRoundResponse(
        Long roundId,
        LocalDateTime startedAt,
        LocalDateTime expiresAt,
        long remainingSeconds,
        RoadMakingProblemResponse problem
) {
    public static StartRoadMakingRoundResponse of(RoadMakingRound round, RoadMakingProblemResponse problem, LocalDateTime now) {
        long remaining = Math.max(0, Duration.between(now, round.getExpiresAt()).getSeconds());
        return new StartRoadMakingRoundResponse(round.getId(), round.getStartedAt(), round.getExpiresAt(), remaining, problem);
    }
}