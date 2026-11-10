package com.example.gifserverv2.domain.auth.dto.request;

import com.example.gifserverv2.domain.user.entity.ClientRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "학생 추가 정보 입력 요청")
public record ClientAdditionalInfoRequest(
        @Schema(description = "팀 내 역할. LEADER(팀장) / MEMBER(팀원)", example = "LEADER")
        @NotNull ClientRole clientRole) {
}
