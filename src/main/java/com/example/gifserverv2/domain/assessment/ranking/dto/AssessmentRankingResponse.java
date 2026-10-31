package com.example.gifserverv2.domain.assessment.ranking.dto;

import java.util.List;

public record AssessmentRankingResponse(
        List<RankingEntry> rankings,
        RankingEntry me
) {
    public record RankingEntry(
            int rank,
            String name,
            Integer grade,
            int correctCount,
            int totalCount,
            int earnedCoin
    ) {
    }
}
