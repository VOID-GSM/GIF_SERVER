package com.example.gifserverv2.domain.engagement.dto.response;

import com.example.gifserverv2.domain.engagement.entity.ProjectComment;
import com.example.gifserverv2.domain.project.entity.Project;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "내가 쓴 댓글")
public record MyCommentResponse(
        @Schema(description = "댓글 ID", example = "10")
        Long id,
        @Schema(description = "댓글 내용", example = "멋진 프로젝트네요!")
        String content,
        @Schema(description = "작성 시각 (UTC, ISO-8601)", example = "2026-11-09T01:23:45Z")
        Instant createdAt,
        @Schema(description = "관리자가 숨긴 댓글인지. 숨긴 댓글은 프로젝트 댓글 목록에 보이지 않는다", example = "false")
        boolean hidden,
        @Schema(description = "프로젝트 ID", example = "1")
        Long projectId,
        @Schema(description = "프로젝트 이름", example = "GIF")
        String projectName,
        @Schema(description = "팀 이름", example = "VOID")
        String teamName
) {

    public static MyCommentResponse from(ProjectComment c, Project project) {
        return new MyCommentResponse(c.getId(), c.getContent(), c.getCreatedAt(), c.isHidden(),
                project.getId(), project.getName(), project.getTeamName());
    }
}
