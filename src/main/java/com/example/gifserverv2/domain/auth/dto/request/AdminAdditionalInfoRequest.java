package com.example.gifserverv2.domain.auth.dto.request;

import com.example.gifserverv2.domain.user.entity.AdminRole;
import jakarta.validation.constraints.NotNull;

public record AdminAdditionalInfoRequest(
        @NotNull AdminRole adminRole,
        String adminTeam) {
}
