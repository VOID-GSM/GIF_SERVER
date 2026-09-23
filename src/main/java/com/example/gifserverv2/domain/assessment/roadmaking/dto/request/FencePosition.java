package com.example.gifserverv2.domain.assessment.roadmaking.dto.request;

import jakarta.validation.constraints.Min;

public record FencePosition(@Min(0) int row, @Min(0) int col) {
}