package com.example.gifserverv2.domain.engagement.dto.response;

import com.example.gifserverv2.domain.engagement.entity.ProjectComment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "관리자용 댓글")
public record AdminCommentResponse(
        @Schema(description = "댓글 ID", example = "10")
        Long id,
        @Schema(description = "프로젝트 ID", example = "1")
        Long projectId,
        @Schema(description = "작성자 userId", example = "12")
        Long userId,
        @Schema(description = "작성자 이름 (작성 당시)", example = "홍길동")
        String authorName,
        @Schema(description = "댓글 내용", example = "멋진 프로젝트네요!")
        String content,
        @Schema(description = "작성 시각 (UTC, ISO-8601)", example = "2026-11-09T01:23:45Z")
        Instant createdAt,
        @Schema(description = "숨김 여부", example = "false")
        boolean hidden
) {

    public static AdminCommentResponse from(ProjectComment c) {
        return new AdminCommentResponse(c.getId(), c.getProjectId(), c.getUserId(), c.getAuthorName(),
                c.getContent(), c.getCreatedAt(), c.isHidden());
    }
}