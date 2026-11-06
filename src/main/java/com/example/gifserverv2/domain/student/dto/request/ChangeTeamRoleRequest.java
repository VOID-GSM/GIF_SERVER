package com.example.gifserverv2.domain.student.dto.request;

import com.example.gifserverv2.domain.user.entity.ClientRole;
import jakarta.validation.constraints.NotNull;

public record ChangeTeamRoleRequest(
        @NotNull(message = "프로젝트 ID는 필수입니다.")
        Long projectId,

        @NotNull(message = "역할은 필수입니다.")
        ClientRole role
) {
}
