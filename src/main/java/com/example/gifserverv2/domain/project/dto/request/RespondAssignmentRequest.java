package com.example.gifserverv2.domain.project.dto.request;

import com.example.gifserverv2.domain.project.entity.ProjectTeacherAssignment.AssignmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RespondAssignmentRequest(
        @NotNull(message = "수락/거절 상태는 필수입니다.")
        AssignmentStatus status,

        @Size(max = 200, message = "거절 사유는 200자 이내로 입력해주세요.")
        String rejectReason
) {
}