package com.example.gifserverv2.domain.auth.dto.request;

import com.example.gifserverv2.domain.user.entity.AdminRole;
import com.example.gifserverv2.domain.user.entity.ClientRole;
import jakarta.validation.constraints.Pattern;

public record UpdateCurrentUserRequest(
        String name,
        @Pattern(regexp = "\\d*", message = "studentNumber must be numeric")
        String studentNumber,
        AdminRole adminRole,
        String adminTeam,
        ClientRole clientRole
) {
}
