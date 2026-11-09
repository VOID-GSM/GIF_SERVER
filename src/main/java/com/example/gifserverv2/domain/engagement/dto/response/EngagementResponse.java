package com.example.gifserverv2.domain.engagement.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로젝트 반응 요약")
public record EngagementResponse(
        @Schema(description = "조회수", example = "340")
        long views,
        @Schema(description = "좋아요 수", example = "12")
        long likes,
        @Schema(description = "댓글 수 (숨김·삭제 제외)", example = "5")
        long commentsCount,
        @Schema(description = "내가 좋아요를 눌렀는지", example = "true")
        boolean likedByMe
) {}