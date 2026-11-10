package com.example.gifserverv2.domain.project.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.gifserverv2.domain.project.entity.Project;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로젝트 목록 항목")
public record ListProjectResponse(
        @Schema(description = "프로젝트 ID", example = "1")
        Long id,
        @Schema(description = "프로젝트 이름", example = "GIF")
        String name,
        @Schema(description = "팀 이름", example = "VOID")
        String teamName,
        @Schema(description = "로고 이미지 URL. 없으면 null", example = "/files/project-logos/abc.png")
        String logo,
        @Schema(description = "학년", example = "2")
        Integer grade,
        @Schema(description = "좋아요 수", example = "12")
        long likeCount,
        @Schema(description = "내가 좋아요를 눌렀는지. 비로그인이면 false", example = "false")
        @JsonProperty("isLiked") boolean isLiked,
        @Schema(description = "조회수", example = "340")
        long viewCount,
        @Schema(description = "댓글 수 (숨김·삭제 제외)", example = "5")
        long commentCount
) {
    public static ListProjectResponse from(Project project, long likeCount, boolean isLiked,
                                           long viewCount, long commentCount) {
        return new ListProjectResponse(
                project.getId(), project.getName(), project.getTeamName(),
                project.getLogo(), project.getGrade(),
                likeCount, isLiked, viewCount, commentCount
        );
    }
}