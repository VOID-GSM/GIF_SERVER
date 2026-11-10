package com.example.gifserverv2.domain.score.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "평가 항목 수정 요청. 생략한 값은 유지된다")
public record PatchScoreItemRequest(
        @Schema(description = "항목 이름 (공백 불가, 최대 100자)", example = "기술 완성도")
        @Pattern(regexp = ".*\\S.*", message = "항목 이름은 비어 있을 수 없습니다.")
        @Size(max = 100, message = "항목 이름은 100자 이하여야 합니다.")
        String name,
        @Schema(description = "평가 기준 설명 (최대 1000자)", example = "핵심 기능이 동작하는가")
        @Size(max = 1000, message = "평가 기준은 1000자 이하여야 합니다.")
        String criteria,
        @Schema(description = "선택지 점수 목록. 1~10개, 각 0~1000, 중복 불가, 하나 이상은 1점 이상", example = "[40, 32, 24, 16, 8]")
        @Size(min = 1, max = ScoreOptionRules.MAX_OPTIONS, message = "선택지 점수는 1개 이상 " + ScoreOptionRules.MAX_OPTIONS + "개 이하여야 합니다.")
        List<@NotNull(message = "선택지 점수는 비어 있을 수 없습니다.")
             @Min(value = 0, message = "선택지 점수는 0 이상이어야 합니다.")
             @Max(value = ScoreOptionRules.MAX_SCORE, message = "선택지 점수는 " + ScoreOptionRules.MAX_SCORE + " 이하여야 합니다.")
             Integer> options,
        @Schema(description = "표시 순서 (0 이상)", example = "1")
        @Min(value = 0, message = "표시 순서는 0 이상이어야 합니다.")
        Integer displayOrder
) {
}
