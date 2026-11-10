package com.example.gifserverv2.domain.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "담당 교사 지정 요청")
public record AssignProjectTeacherRequest(
        @Schema(description = "프로젝트 ID", example = "1")
        @NotNull(message = "프로젝트 ID는 필수 항목입니다.") Long projectId,
        @Schema(description = "지정할 선생님 userId", example = "7")
        @NotNull(message = "선생님 ID는 필수 항목입니다.") Long teacherId
) {
}
