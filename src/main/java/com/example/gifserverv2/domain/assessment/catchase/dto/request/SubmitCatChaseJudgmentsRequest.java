package com.example.gifserverv2.domain.assessment.catchase.dto.request;

import com.example.gifserverv2.domain.assessment.catchase.entity.ConfidenceLevel;
import com.example.gifserverv2.domain.assessment.catchase.entity.Judgment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "고양이 술래잡기 판단 제출")
public record SubmitCatChaseJudgmentsRequest(
        @Schema(description = "고양이 칸마다 하나씩의 판단 목록")
        @NotEmpty(message = "판단 결과는 최소 1개 이상이어야 합니다.")
        @Valid
        List<JudgmentItem> judgments
) {
    @Schema(description = "고양이 칸 하나에 대한 판단")
    public record JudgmentItem(
            @Schema(description = "고양이 칸 번호 (0~35)", example = "14")
            @NotNull(message = "위치는 필수입니다.")
            Integer position,

            @Schema(description = "판단. FOUND(쥐가 있었다) / MISSED(없었다)", example = "FOUND")
            @NotNull(message = "판단은 필수입니다.")
            Judgment judgment,

            @Schema(description = "확신도. VERY_CONFIDENT / CONFIDENT / SOMEWHAT_UNSURE / UNSURE", example = "CONFIDENT")
            @NotNull(message = "확신도는 필수입니다.")
            ConfidenceLevel confidence
    ) {}
}
