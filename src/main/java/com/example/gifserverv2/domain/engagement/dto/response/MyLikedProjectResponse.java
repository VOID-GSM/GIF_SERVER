package com.example.gifserverv2.domain.engagement.dto.response;

import com.example.gifserverv2.domain.engagement.entity.ProjectLike;
import com.example.gifserverv2.domain.project.entity.Project;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.ZoneId;

@Schema(description = "좋아요한 프로젝트")
public record MyLikedProjectResponse(
        @Schema(description = "프로젝트 ID", example = "1")
        Long projectId,
        @Schema(description = "프로젝트 이름", example = "GIF")
        String projectName,
        @Schema(description = "팀 이름", example = "VOID")
        String teamName,
        @Schema(description = "로고(썸네일) 이미지 URL. 없으면 null", example = "/files/project-logos/abc.png")
        String logo,
        @Schema(description = "좋아요 수", example = "12")
        long likeCount,
        @Schema(description = "좋아요 누른 시각 (UTC, ISO-8601)", example = "2026-11-09T01:23:45Z")
        Instant likedAt
) {

    // ProjectLike.createdAt 은 서버 시간대(Asia/Seoul)의 LocalDateTime 이라, 댓글 작성 시각처럼 UTC Instant 로 바꿔 내려준다
    private static final ZoneId SERVER_ZONE = ZoneId.of("Asia/Seoul");

    public static MyLikedProjectResponse from(ProjectLike like, Project project, long likeCount) {
        return new MyLikedProjectResponse(project.getId(), project.getName(), project.getTeamName(),
                project.getLogo(), likeCount, like.getCreatedAt().atZone(SERVER_ZONE).toInstant());
    }
}
