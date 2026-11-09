package com.example.gifserverv2.domain.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "지도 교사 지정 요청")
public record AssignAdvisorTeacherRequest(
        @Schema(description = "지정할 선생님 userId", example = "7")
        @NotNull(message = "선생님 ID는 필수입니다.")
        Long advisorTeacherId
) {
}
