package com.example.gifserverv2.domain.project.dto.response;

import com.example.gifserverv2.domain.project.repository.ProjectStatsRepository.ProjectStatsRow;

// 쿼리 결과 한 줄(프로젝트 1개)을 담는 응답 객체
public record ProjectStatsResponse(
        Long projectId,
        String name,
        String teamName,
        Integer grade,
        Integer classNo,
        long viewCount,
        long likeCount,
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