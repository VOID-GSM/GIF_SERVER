package com.example.gifserverv2.domain.student.dto.request;

import jakarta.validation.constraints.NotNull;

public record AssignTeamRequest(
        @NotNull(message = "프로젝트 ID는 필수입니다.")
        Long projectId
) {
}
