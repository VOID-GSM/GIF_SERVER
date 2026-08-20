package com.example.gifserverv2.domain.project.dto.response;

import com.example.gifserverv2.domain.project.entity.ProjectTeacherAssignment.AssignmentStatus;
import com.example.gifserverv2.domain.user.entity.AdminRole;

import java.util.List;

public record TeacherListResponse(
        Long id,
        String email,
        String name,
        AdminRole adminRole,
        String adminTeam,
        boolean isGradeHead,
        AssignmentInfo assignmentInfo,
        boolean isScoreSubmitted,
        List<UnsubmittedProjectInfo> unsubmittedProjects
) {
    public record AssignmentInfo(
            AssignmentStatus status,
            String rejectReason
    ) {}

    public record UnsubmittedProjectInfo(
            Long projectId,
            String projectName,
            String teamName
    ) {}
}