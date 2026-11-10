package com.example.gifserverv2.domain.engagement.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좋아요 처리 결과")
public record LikeResponse(
        @Schema(description = "처리 후 내 좋아요 상태", example = "true")
        boolean liked,
        @Schema(description = "처리 후 좋아요 수", example = "13")
        long likes
) {}