package com.example.gifserverv2.domain.project.dto.request;

import jakarta.validation.constraints.NotNull;

public record TransferLeaderRequest (
        @NotNull(message = "양도할 팀원 ID는 필수입니다.")
        Long newLeaderUserId
){}
