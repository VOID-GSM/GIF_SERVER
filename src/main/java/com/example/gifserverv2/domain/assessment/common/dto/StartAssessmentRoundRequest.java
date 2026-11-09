package com.example.gifserverv2.domain.assessment.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "역량검사 라운드 시작 요청")
public record StartAssessmentRoundRequest(
        @Schema(description = "문항 수 (3~10)", example = "10")
        @NotNull(message = "문항 수는 필수입니다.")
        @Min(value = MIN_QUESTION_COUNT, message = "문항 수는 3개 이상이어야 합니다.")
        @Max(value = MAX_QUESTION_COUNT, message = "문항 수는 10개 이하여야 합니다.")
        Integer questionCount
) {
    public static final int MIN_QUESTION_COUNT = 3;
    public static final int MAX_QUESTION_COUNT = 10;
}
