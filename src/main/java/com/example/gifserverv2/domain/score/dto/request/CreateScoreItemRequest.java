package com.example.gifserverv2.domain.score.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateScoreItemRequest(
        @NotBlank(message = "항목 이름은 필수입니다.")
        @Size(max = 100, message = "항목 이름은 100자 이하여야 합니다.")
        String name,
        @Size(max = 1000, message = "평가 기준은 1000자 이하여야 합니다.")
        String criteria,
        @NotEmpty(message = "선택지 점수는 1개 이상이어야 합니다.")
        @Size(max = ScoreOptionRules.MAX_OPTIONS, message = "선택지 점수는 " + ScoreOptionRules.MAX_OPTIONS + "개 이하여야 합니다.")
        List<@NotNull(message = "선택지 점수는 비어 있을 수 없습니다.")
             @Min(value = 0, message = "선택지 점수는 0 이상이어야 합니다.")
             @Max(value = ScoreOptionRules.MAX_SCORE, message = "선택지 점수는 " + ScoreOptionRules.MAX_SCORE + " 이하여야 합니다.")
             Integer> options,
        @Min(value = 0, message = "표시 순서는 0 이상이어야 합니다.")
        Integer displayOrder
) {
}
