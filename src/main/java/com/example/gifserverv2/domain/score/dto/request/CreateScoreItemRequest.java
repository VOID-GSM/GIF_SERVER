package com.example.gifserverv2.domain.score.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateScoreItemRequest(
        @NotBlank(message = "항목 이름은 필수입니다.")
        @Size(max = 100, message = "항목 이름은 100자 이하여야 합니다.")
        String name,
        @Size(max = 1000, message = "평가 기준은 1000자 이하여야 합니다.")
        String criteria,
        @NotNull(message = "배점은 필수입니다.")
        @Min(value = 1, message = "배점은 1 이상이어야 합니다.")
        @Max(value = 1000, message = "배점은 1000 이하여야 합니다.")
        Integer maxScore,
        @Min(value = 0, message = "표시 순서는 0 이상이어야 합니다.")
        Integer displayOrder
) {
}
