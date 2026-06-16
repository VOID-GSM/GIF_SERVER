package com.example.gifserverv2.domain.score.dto.response;

public record ScoreSummaryResponse(
        Long projectId,
        String teamName,
        double averageScore,
        int scoreCount
) {}
