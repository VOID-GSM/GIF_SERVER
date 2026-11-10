package com.example.gifserverv2.domain.student.dto.request;

import com.example.gifserverv2.domain.user.entity.ClientRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "팀 내 역할 변경 요청")
public record ChangeTeamRoleRequest(
        @Schema(description = "프로젝트 ID", example = "1")
        @NotNull(message = "프로젝트 ID는 필수입니다.")
        Long projectId,

        @Schema(description = "역할 LEADER / MEMBER", example = "LEADER")
        @NotNull(message = "역할은 필수입니다.")
        ClientRole role
) {
}
