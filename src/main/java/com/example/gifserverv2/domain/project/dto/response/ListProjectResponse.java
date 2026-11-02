package com.example.gifserverv2.domain.project.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.gifserverv2.domain.project.entity.Project;

public record ListProjectResponse(
        Long id,
        String name,
        String teamName,
        String logo,
        Integer grade,
        long likeCount,
        @JsonProperty("isLiked") boolean isLiked
) {
    public static ListProjectResponse from(Project project, long likeCount, boolean isLiked) {
        return new ListProjectResponse(
                project.getId(),
                project.getName(),
                project.getTeamName(),
                project.getLogo(),
                project.getGrade(),
                likeCount,
                isLiked
        );
    }
}
