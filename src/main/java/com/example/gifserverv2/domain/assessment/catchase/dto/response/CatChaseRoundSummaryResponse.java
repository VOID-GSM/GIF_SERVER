package com.example.gifserverv2.domain.assessment.catchase.dto.response;

import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseRound;

import java.time.LocalDateTime;

public record CatChaseRoundSummaryResponse(
        Long roundId,
        int questionCount,
        int answeredCount,
        int correctCount,
        boolean finished,
        boolean completed,
        int earnedCoin,
        LocalDateTime createdAt
) {
    public static CatChaseRoundSummaryResponse from(CatChaseRound round) {
        return new CatChaseRoundSummaryResponse(
                round.getId(),
                round.getQuestionCount(),
                round.getAnsweredCount(),
                round.getCorrectCount(),
                !round.isInProgress(),
                round.isCompleted(),
                round.getEarnedCoin(),
                round.getCreatedAt()
        );
    }
}
