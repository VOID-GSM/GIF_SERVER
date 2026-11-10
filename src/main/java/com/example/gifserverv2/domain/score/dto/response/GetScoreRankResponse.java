package com.example.gifserverv2.domain.score.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로젝트 순위")
public record GetScoreRankResponse(
        @Schema(description = "순위. 동점은 같은 순위", example = "1")
        int rank,
        @Schema(description = "팀 이름", example = "VOID")
        String teamName,
        @Schema(description = "총점 평균 (반올림)", example = "235")
        int totalScore
) {}
