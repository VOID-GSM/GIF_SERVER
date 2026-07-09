package com.example.gifserverv2.domain.score.dto.response;

public record GetScoreSummaryResponse(
        Long projectId,
        String teamName,
        double averageScore,
        int scoreCount
) {}
