package com.example.gifserverv2.domain.project.dto.request;

import jakarta.validation.constraints.NotNull;

public record AssignProjectTeacherRequest(
        @NotNull(message = "프로젝트 ID는 필수 항목입니다.") Long projectId,
        @NotNull(message = "선생님 ID는 필수 항목입니다.") Long teacherId
) {
}
