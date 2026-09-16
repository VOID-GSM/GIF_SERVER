package com.example.gifserverv2.domain.assessment.catchase.dto.response;

import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseRound;

import java.time.LocalDateTime;

public record CatChaseRoundSummaryResponse(
        Long roundId,
        int mouseCount,
        int catCount,
        boolean answered,
        LocalDateTime createdAt
) {
    public static CatChaseRoundSummaryResponse from(CatChaseRound round, boolean answered) {
        return new CatChaseRoundSummaryResponse(
                round.getId(),
                round.getMousePositions().size(),
                round.getCatPositions().size(),
                answered,
                round.getCreatedAt()
        );
    }
}
