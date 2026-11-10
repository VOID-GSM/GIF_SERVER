package com.example.gifserverv2.domain.assessment.roadmaking.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "격자 위치")
public record Position(
        @Schema(description = "행 (0부터)", example = "2")
        int row,
        @Schema(description = "열 (0부터)", example = "3")
        int col
) {
}