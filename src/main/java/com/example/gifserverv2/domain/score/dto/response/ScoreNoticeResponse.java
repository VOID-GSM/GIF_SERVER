package com.example.gifserverv2.domain.score.dto.response;

import java.time.Instant;
import java.util.List;

public record ScoreNoticeResponse(
        boolean isPublished,
        Instant publishedAt,
        List<ScoreSummaryResponse> scores
) {}
