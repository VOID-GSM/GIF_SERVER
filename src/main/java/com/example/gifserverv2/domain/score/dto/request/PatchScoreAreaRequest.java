package com.example.gifserverv2.domain.score.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PatchScoreAreaRequest(
        @Pattern(regexp = ".*\\S.*", message = "영역 이름은 비어 있을 수 없습니다.")
        @Size(max = 100, message = "영역 이름은 100자 이하여야 합니다.")
        String name,
        @Min(value = 0, message = "표시 순서는 0 이상이어야 합니다.")
        Integer displayOrder
) {
}
