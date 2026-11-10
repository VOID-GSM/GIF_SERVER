package com.example.gifserverv2.domain.project.dto.response;

import com.example.gifserverv2.domain.project.repository.ProjectStatsRepository.ProjectStatsRow;
import io.swagger.v3.oas.annotations.media.Schema;

// 쿼리 결과 한 줄(프로젝트 1개)을 담는 응답 객체
@Schema(description = "프로젝트 통계 항목")
public record ProjectStatsResponse(
        @Schema(description = "프로젝트 ID", example = "1")
        Long projectId,
        @Schema(description = "프로젝트 이름", example = "GIF")
        String name,
        @Schema(description = "팀 이름", example = "VOID")
        String teamName,
        @Schema(description = "학년", example = "2")
        Integer grade,
        @Schema(description = "반. 프로젝트에 지정된 반이 없으면 팀원 학번에서 계산한 값", example = "1")
        Integer classNo,
        @Schema(description = "조회수", example = "340")
        long viewCount,
        @Schema(description = "좋아요 수", example = "12")
        long likeCount,
        @Schema(description = "댓글 수", example = "5")
        long commentCount
) {
    // Repository가 돌려주는 projection을 응답으로 변환
    public static ProjectStatsResponse from(ProjectStatsRow row) {
        return new ProjectStatsResponse(
                row.getProjectId(), row.getName(), row.getTeamName(),
                row.getGrade(), row.getClassNo(),
                row.getViewCount(), row.getLikeCount(), row.getCommentCount());
    }
}