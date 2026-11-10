package com.example.gifserverv2.domain.assessment.catchase.dto.response;

import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseRound;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "고양이 술래잡기 라운드 결과")
public record CatChaseRoundResultResponse(
        @Schema(description = "라운드 ID", example = "11")
        Long roundId,
        @Schema(description = "전체 문항 수", example = "10")
        int questionCount,
        @Schema(description = "응답한 문항 수", example = "10")
        int answeredCount,
        @Schema(description = "맞힌 문항 수", example = "7")
        int correctCount,
        @Schema(description = "끝까지 진행했는지. 중간 종료(이탈)면 false", example = "true")
        boolean completed,
        @Schema(description = "지급된 코인", example = "90")
        int earnedCoin
) {
    public static CatChaseRoundResultResponse from(CatChaseRound round) {
        return new CatChaseRoundResultResponse(
                round.getId(),
                round.getQuestionCount(),
                round.getAnsweredCount(),
                round.getCorrectCount(),
                round.isCompleted(),
                round.getEarnedCoin()
        );
    }
}
