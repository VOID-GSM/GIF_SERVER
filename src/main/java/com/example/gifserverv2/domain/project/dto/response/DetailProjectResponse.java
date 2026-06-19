package com.example.gifserverv2.domain.project.dto.response;

import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.user.entity.UserEntity;

import java.util.List;
import java.util.Map;

public record DetailProjectResponse(
        Long id,
        String name,
        String teamName,
        String description,
        String logo,
        Integer grade,
        List<MemberInfo> members
) {
    public record MemberInfo(Long userId, String name, String studentNumber, String role) {}

    public static DetailProjectResponse from(Project project, Map<Long, UserEntity> userMap) {
        List<MemberInfo> memberInfos = project.getMembers().stream()
                .map(m -> {
                    UserEntity user = userMap.get(m.getUserId());
                    String name = user != null ? user.getName() : null;
                    String studentNumber = user != null ? user.getStudentNumber() : null;
                    return new MemberInfo(m.getUserId(), name, studentNumber, m.getRole().name());
                })
                .toList();

        return new DetailProjectResponse(
                project.getId(),
                project.getName(),
                project.getTeamName(),
                project.getDescription(),
                project.getLogo(),
                project.getGrade(),
                memberInfos
        );
    }
}
