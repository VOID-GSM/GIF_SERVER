package com.example.gifserverv2.domain.score.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateScoreAreaRequest(
        @NotBlank(message = "영역 이름은 필수입니다.")
        @Size(max = 100, message = "영역 이름은 100자 이하여야 합니다.")
        String name,
        @Min(value = 0, message = "표시 순서는 0 이상이어야 합니다.")
        Integer displayOrder
) {
}
