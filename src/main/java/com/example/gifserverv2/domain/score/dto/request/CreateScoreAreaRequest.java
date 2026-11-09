package com.example.gifserverv2.domain.score.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "평가 영역 생성 요청")
public record CreateScoreAreaRequest(
        @Schema(description = "영역 이름 (최대 100자)", example = "기술")
        @NotBlank(message = "영역 이름은 필수입니다.")
        @Size(max = 100, message = "영역 이름은 100자 이하여야 합니다.")
        String name,
        @Schema(description = "표시 순서 (0 이상, 선택). 생략하면 맨 뒤", example = "0")
        @Min(value = 0, message = "표시 순서는 0 이상이어야 합니다.")
        Integer displayOrder
) {
}
