package com.example.gifserverv2.domain.auth.dto.request;

import com.example.gifserverv2.domain.user.entity.AdminRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminAdditionalInfoRequest(
        @NotNull(message = "선생님 역할은 필수입니다.") AdminRole adminRole,
        @NotBlank(message = "이름은 필수입니다.") String name,
        boolean gradeHead
) {
}