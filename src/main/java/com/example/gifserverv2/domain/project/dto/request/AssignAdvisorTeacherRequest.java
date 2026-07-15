package com.example.gifserverv2.domain.project.dto.request;

import jakarta.validation.constraints.NotNull;

public record AssignAdvisorTeacherRequest(
        @NotNull(message = "선생님 ID는 필수입니다.")
        Long advisorTeacherId
) {
}
