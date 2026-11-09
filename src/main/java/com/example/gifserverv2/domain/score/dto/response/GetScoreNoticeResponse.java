package com.example.gifserverv2.domain.score.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "성적 공지")
public record GetScoreNoticeResponse(
        @Schema(description = "공지 존재 여부", example = "true")
        boolean isPublished,
        @Schema(description = "공지 시각 (UTC). 공지가 없으면 null", example = "2026-12-30T01:00:00Z")
        Instant publishedAt,
        @Schema(description = "프로젝트별 점수 요약. 공지가 없으면 빈 배열")
        List<GetScoreSummaryResponse> scores
) {}
