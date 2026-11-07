package com.example.gifserverv2.domain.score.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 요청에 담긴 항목만 저장(없으면 생성, 있으면 수정)한다. 담기지 않은 항목의 기존 점수는 그대로 둔다.
 */
public record SaveItemScoresRequest(
        @NotEmpty(message = "채점할 항목이 없습니다.")
        List<@NotNull(message = "채점 항목은 비어 있을 수 없습니다.") @Valid ItemScore> scores
) {
    public record ItemScore(
            @NotNull(message = "itemId는 필수입니다.")
            Long itemId,
            @NotNull(message = "score는 필수입니다.")
            Integer score
    ) {
    }
}
