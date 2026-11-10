package com.example.gifserverv2.domain.assessment.roadmaking.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(description = "울타리 위치")
public record FencePosition(
        @Schema(description = "행 (0부터)", example = "2")
        @Min(0) int row,
        @Schema(description = "열 (0부터)", example = "3")
        @Min(0) int col
) {
}