package com.example.gifserverv2.domain.project.dto.response;

import com.example.gifserverv2.domain.project.entity.ProjectTeacherAssignment.AssignmentStatus;

public record MyTeacherAssignmentResponse(
        Long assignmentId,
        Long projectId,
        String projectName,
        String teamName,
        AssignmentStatus status,
        String rejectReason
) {
}