package com.example.gifserverv2.domain.score.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공지 시점의 프로젝트 점수 요약")
public record GetScoreSummaryResponse(
        @Schema(description = "프로젝트 ID", example = "1")
        Long projectId,
        @Schema(description = "팀 이름", example = "VOID")
        String teamName,
        @Schema(description = "선생님별 총점의 평균", example = "234.5")
        double averageScore,
        @Schema(description = "채점한 선생님 수", example = "4")
        int scoreCount
) {}
