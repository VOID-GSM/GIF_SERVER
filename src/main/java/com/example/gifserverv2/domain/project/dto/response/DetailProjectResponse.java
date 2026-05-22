package com.example.gifserverv2.domain.project.dto.response;

import com.example.gifserverv2.domain.project.entity.Project;

import java.util.List;

public record DetailProjectResponse(
        Long id,
        String name,
        String teamName,
        String description,
        String logoPath,
        List<MemberInfo> members
) {
    public record MemberInfo(Long userId, String role) {}

    public static DetailProjectResponse from(Project project) {
        List<MemberInfo> memberInfos = project.getMembers().stream()
                .map(m -> new MemberInfo(m.getUserId(), m.getRole().name()))
                .toList();

        return new DetailProjectResponse(
                project.getId(),
                project.getName(),
                project.getTeamName(),
                project.getDescription(),
                project.getLogoPath(),
                memberInfos
        );
    }
}
