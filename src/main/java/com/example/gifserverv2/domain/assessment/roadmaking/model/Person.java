package com.example.gifserverv2.domain.assessment.roadmaking.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사람 (차량 목적지)")
public record Person(
        @Schema(description = "색. 같은 색 차량이 도착해야 한다", example = "RED")
        Color color,
        @Schema(description = "위치")
        Position position
) {
}