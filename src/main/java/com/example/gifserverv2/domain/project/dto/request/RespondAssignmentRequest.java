package com.example.gifserverv2.domain.project.dto.request;

import com.example.gifserverv2.domain.project.entity.ProjectTeacherAssignment.AssignmentStatus;
import jakarta.validation.constraints.NotNull;

public record RespondAssignmentRequest(
        @NotNull(message = "수락/거절 상태는 필수입니다.") AssignmentStatus status,
        String rejectReason
) {
}
