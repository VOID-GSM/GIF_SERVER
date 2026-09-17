package com.example.gifserverv2.domain.assessment.catchase.dto.request;

import com.example.gifserverv2.domain.assessment.catchase.entity.ConfidenceLevel;
import com.example.gifserverv2.domain.assessment.catchase.entity.Judgment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SubmitCatChaseJudgmentsRequest(
        @NotEmpty(message = "판단 결과는 최소 1개 이상이어야 합니다.")
        @Valid
        List<JudgmentItem> judgments
) {
    public record JudgmentItem(
            @NotNull(message = "위치는 필수입니다.")
            Integer position,

            @NotNull(message = "판단은 필수입니다.")
            Judgment judgment,

            @NotNull(message = "확신도는 필수입니다.")
            ConfidenceLevel confidence
    ) {}
}
