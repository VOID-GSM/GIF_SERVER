package com.example.gifserverv2.domain.assessment.ranking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "역량검사 랭킹")
public record AssessmentRankingResponse(
        @Schema(description = "문항 수", example = "10")
        int questionCount,
        @Schema(description = "상위 랭킹")
        List<RankingEntry> rankings,
        @Schema(description = "내 순위. 기록이 없으면 null")
        RankingEntry me
) {
    @Schema(description = "랭킹 항목")
    public record RankingEntry(
            @Schema(description = "순위", example = "1")
            int rank,
            @Schema(description = "이름", example = "홍길동")
            String name,
            @Schema(description = "학년", example = "2")
            Integer grade,
            @Schema(description = "맞힌 문항 수", example = "9")
            int correctCount,
            @Schema(description = "전체 문항 수", example = "10")
            int totalCount,
            @Schema(description = "획득 코인", example = "110")
            int earnedCoin
    ) {
    }
}
