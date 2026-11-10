package com.example.gifserverv2.domain.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "팀장 양도 요청")
public record TransferLeaderRequest (
        @Schema(description = "새 팀장이 될 팀원의 userId", example = "15")
        @NotNull(message = "양도할 팀원 ID는 필수입니다.")
        Long newLeaderUserId
){}
