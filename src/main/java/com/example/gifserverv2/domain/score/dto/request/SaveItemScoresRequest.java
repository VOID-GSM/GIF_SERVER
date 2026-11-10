package com.example.gifserverv2.domain.score.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 요청에 담긴 항목만 저장(없으면 생성, 있으면 수정)한다. 담기지 않은 항목의 기존 점수는 그대로 둔다.
 */
@Schema(description = "항목별 점수 저장 요청. 담긴 항목만 저장된다")
public record SaveItemScoresRequest(
        @Schema(description = "저장할 항목 점수 목록 (1개 이상, 같은 항목 중복 불가)")
        @NotEmpty(message = "채점할 항목이 없습니다.")
        List<@NotNull(message = "채점 항목은 비어 있을 수 없습니다.") @Valid ItemScore> scores
) {
    @Schema(description = "항목 점수")
    public record ItemScore(
            @Schema(description = "항목 ID", example = "4")
            @NotNull(message = "itemId는 필수입니다.")
            Long itemId,
            @Schema(description = "점수. 항목의 선택지(options) 중 하나", example = "32")
            @NotNull(message = "score는 필수입니다.")
            Integer score
    ) {
    }
}
