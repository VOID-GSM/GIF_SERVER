package com.example.gifserverv2.domain.assessment.catchase.dto.response;

import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseRound;

public record StartCatChaseRoundResponse(
        Long roundId,
        int gridSize,
        int questionCount,
        CatChaseQuestionResponse question
) {
    public static StartCatChaseRoundResponse of(CatChaseRound round, CatChaseQuestionResponse question) {
        return new StartCatChaseRoundResponse(round.getId(), CatChaseRound.GRID_SIZE, round.getQuestionCount(), question);
    }
}
