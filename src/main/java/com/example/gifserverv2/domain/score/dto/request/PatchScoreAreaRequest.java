package com.example.gifserverv2.domain.score.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "평가 영역 수정 요청. 생략한 값은 유지된다")
public record PatchScoreAreaRequest(
        @Schema(description = "영역 이름 (공백 불가, 최대 100자)", example = "기술")
        @Pattern(regexp = ".*\\S.*", message = "영역 이름은 비어 있을 수 없습니다.")
        @Size(max = 100, message = "영역 이름은 100자 이하여야 합니다.")
        String name,
        @Schema(description = "표시 순서 (0 이상)", example = "1")
        @Min(value = 0, message = "표시 순서는 0 이상이어야 합니다.")
        Integer displayOrder
) {
}
