package com.example.gifserverv2.domain.assessment.catchase.dto.response;

import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseRound;

/**
 * nextQuestion: 남은 문항이 있으면 다음 문항, 마지막 문항이었으면 null
 * result: 마지막 문항이었으면 라운드 결과(코인 포함), 아니면 null
 */
public record SubmitCatChaseJudgmentsResponse(
        boolean correct,
        int answeredCount,
        int correctCount,
        CatChaseQuestionResponse nextQuestion,
        CatChaseRoundResultResponse result
) {
    public static SubmitCatChaseJudgmentsResponse of(boolean correct, CatChaseRound round,
                                                     CatChaseQuestionResponse nextQuestion,
                                                     CatChaseRoundResultResponse result) {
        return new SubmitCatChaseJudgmentsResponse(correct, round.getAnsweredCount(), round.getCorrectCount(),
                nextQuestion, result);
    }
}
