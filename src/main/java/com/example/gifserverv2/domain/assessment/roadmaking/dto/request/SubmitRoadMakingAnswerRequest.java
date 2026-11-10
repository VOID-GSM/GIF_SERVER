package com.example.gifserverv2.domain.assessment.roadmaking.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "길 만들기 울타리 배치 제출")
public record SubmitRoadMakingAnswerRequest(
        @Schema(description = "울타리 위치 목록. 개수는 문항의 minFenceCount와 같아야 한다")
        @NotNull List<@Valid @NotNull FencePosition> fences
) {
}