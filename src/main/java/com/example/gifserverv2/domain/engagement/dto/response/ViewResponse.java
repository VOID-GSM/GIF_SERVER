package com.example.gifserverv2.domain.engagement.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "조회수 처리 결과")
public record ViewResponse(
        @Schema(description = "현재 누적 조회수", example = "341")
        long views
) {}