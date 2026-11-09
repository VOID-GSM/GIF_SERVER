package com.example.gifserverv2.domain.assessment.roadmaking.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "차량")
public record Vehicle(
        @Schema(description = "색. RED / BLUE / GREEN / YELLOW / PURPLE / ORANGE", example = "RED")
        Color color,
        @Schema(description = "시작 위치")
        Position position,
        @Schema(description = "진행 방향. UP / RIGHT / DOWN / LEFT", example = "RIGHT")
        Direction direction
) {
}