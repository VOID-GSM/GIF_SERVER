package com.example.gifserverv2.domain.retrospective.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "북마크 처리 결과")
public record BookmarkToggleResponse(
        @Schema(description = "처리 후 북마크 수", example = "4")
        long bookmarkCount,
        @Schema(description = "처리 후 내 북마크 상태", example = "true")
        boolean isBookmarked
) {}
