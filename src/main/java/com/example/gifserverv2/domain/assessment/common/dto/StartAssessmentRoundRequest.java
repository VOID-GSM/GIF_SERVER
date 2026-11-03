package com.example.gifserverv2.domain.assessment.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StartAssessmentRoundRequest(
        @NotNull(message = "문항 수는 필수입니다.")
        @Min(value = MIN_QUESTION_COUNT, message = "문항 수는 3개 이상이어야 합니다.")
        @Max(value = MAX_QUESTION_COUNT, message = "문항 수는 10개 이하여야 합니다.")
        Integer questionCount
) {
    public static final int MIN_QUESTION_COUNT = 3;
    public static final int MAX_QUESTION_COUNT = 10;
}
