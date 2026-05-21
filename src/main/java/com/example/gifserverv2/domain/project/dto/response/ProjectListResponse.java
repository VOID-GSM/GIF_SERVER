package com.example.gifserverv2.domain.project.dto.response;

import com.example.gifserverv2.domain.project.entity.Project;

public record ProjectListResponse(
        Long id,
        String name,
        String teamName,
        String logoPath
) {
    public static ProjectListResponse from(Project project) {
        return new ProjectListResponse(
                project.getId(),
                project.getName(),
                project.getTeamName(),
                project.getLogoPath()
        );
    }
}
