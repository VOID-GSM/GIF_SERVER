package com.example.gifserverv2.domain.project.dto.response;

import com.example.gifserverv2.domain.project.entity.Project;

public record ListProjectResponse(
        Long id,
        String name,
        String teamName,
        String logo,
        Integer grade
) {
    public static ListProjectResponse from(Project project) {
        return new ListProjectResponse(
                project.getId(),
                project.getName(),
                project.getTeamName(),
                project.getLogo(),
                project.getGrade()
        );
    }
}
