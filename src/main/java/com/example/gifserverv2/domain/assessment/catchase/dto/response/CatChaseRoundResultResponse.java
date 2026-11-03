package com.example.gifserverv2.domain.assessment.catchase.dto.response;

import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseRound;

public record CatChaseRoundResultResponse(
        Long roundId,
        int questionCount,
        int answeredCount,
        int correctCount,
        boolean completed,
        int earnedCoin
) {
    public static CatChaseRoundResultResponse from(CatChaseRound round) {
        return new CatChaseRoundResultResponse(
                round.getId(),
                round.getQuestionCount(),
                round.getAnsweredCount(),
                round.getCorrectCount(),
                round.isCompleted(),
                round.getEarnedCoin()
        );
    }
}
