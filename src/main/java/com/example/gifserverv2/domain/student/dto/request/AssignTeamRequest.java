package com.example.gifserverv2.domain.student.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "팀 배정·이동 요청")
public record AssignTeamRequest(
        @Schema(description = "대상 프로젝트 ID", example = "1")
        @NotNull(message = "프로젝트 ID는 필수입니다.")
        Long projectId
) {
}
