package com.example.gifserverv2.domain.assessment.catchase.dto.response;

import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseRound;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * nextQuestion: 남은 문항이 있으면 다음 문항, 마지막 문항이었으면 null
 * result: 마지막 문항이었으면 라운드 결과(코인 포함), 아니면 null
 */
@Schema(description = "고양이 술래잡기 판단 제출 결과")
public record SubmitCatChaseJudgmentsResponse(
        @Schema(description = "이 문항 정답 여부", example = "true")
        boolean correct,
        @Schema(description = "지금까지 응답한 문항 수", example = "3")
        int answeredCount,
        @Schema(description = "지금까지 맞힌 문항 수", example = "2")
        int correctCount,
        @Schema(description = "다음 문항. 마지막 문항이었으면 null")
        CatChaseQuestionResponse nextQuestion,
        @Schema(description = "라운드 결과. 마지막 문항이 아니면 null")
        CatChaseRoundResultResponse result
) {
    public static SubmitCatChaseJudgmentsResponse of(boolean correct, CatChaseRound round,
                                                     CatChaseQuestionResponse nextQuestion,
                                                     CatChaseRoundResultResponse result) {
        return new SubmitCatChaseJudgmentsResponse(correct, round.getAnsweredCount(), round.getCorrectCount(),
                nextQuestion, result);
    }
}
