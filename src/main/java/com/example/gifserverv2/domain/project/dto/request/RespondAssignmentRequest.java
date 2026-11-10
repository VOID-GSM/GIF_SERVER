package com.example.gifserverv2.domain.project.dto.request;

import com.example.gifserverv2.domain.project.entity.ProjectTeacherAssignment.AssignmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "담당 교사 요청 수락·거절")
public record RespondAssignmentRequest(
        @Schema(description = "응답. ACCEPTED(수락) / REJECTED(거절)", example = "ACCEPTED")
        @NotNull(message = "수락/거절 상태는 필수입니다.")
        AssignmentStatus status,

        @Schema(description = "거절 사유 (REJECTED일 때 필수, 최대 200자)", example = "일정이 맞지 않습니다.")
        @Size(max = 200, message = "거절 사유는 200자 이내로 입력해주세요.")
        String rejectReason
) {
}