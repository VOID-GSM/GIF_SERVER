package com.example.gifserverv2.domain.assessment.roadmaking.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SubmitRoadMakingAnswerRequest(@NotNull List<@Valid @NotNull FencePosition> fences) {
}