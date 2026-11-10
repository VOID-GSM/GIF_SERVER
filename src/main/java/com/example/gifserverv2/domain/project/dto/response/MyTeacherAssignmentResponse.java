package com.example.gifserverv2.domain.project.dto.response;

import com.example.gifserverv2.domain.project.entity.ProjectTeacherAssignment.AssignmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내게 온 담당 교사 요청")
public record MyTeacherAssignmentResponse(
        @Schema(description = "배정 요청 ID", example = "5")
        Long assignmentId,
        @Schema(description = "프로젝트 ID", example = "1")
        Long projectId,
        @Schema(description = "프로젝트 이름", example = "GIF")
        String projectName,
        @Schema(description = "팀 이름", example = "VOID")
        String teamName,
        @Schema(description = "상태. PENDING(대기) / ACCEPTED(수락) / REJECTED(거절)", example = "PENDING")
        AssignmentStatus status,
        @Schema(description = "거절 사유. 거절이 아니면 null")
        String rejectReason
) {
}