package com.example.gifserverv2.domain.assessment.catchase.dto.response;

import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseRound;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "고양이 술래잡기 라운드 요약")
public record CatChaseRoundSummaryResponse(
        @Schema(description = "라운드 ID", example = "11")
        Long roundId,
        @Schema(description = "전체 문항 수", example = "10")
        int questionCount,
        @Schema(description = "응답한 문항 수", example = "10")
        int answeredCount,
        @Schema(description = "맞힌 문항 수", example = "7")
        int correctCount,
        @Schema(description = "종료 여부 (완료·이탈 포함)", example = "true")
        boolean finished,
        @Schema(description = "끝까지 진행했는지", example = "true")
        boolean completed,
        @Schema(description = "지급된 코인", example = "90")
        int earnedCoin,
        @Schema(description = "시작 일시", example = "2026-11-09T10:00:00")
        LocalDateTime createdAt
) {
    public static CatChaseRoundSummaryResponse from(CatChaseRound round) {
        return new CatChaseRoundSummaryResponse(
                round.getId(),
                round.getQuestionCount(),
                round.getAnsweredCount(),
                round.getCorrectCount(),
                !round.isInProgress(),
                round.isCompleted(),
                round.getEarnedCoin(),
                round.getCreatedAt()
        );
    }
}
