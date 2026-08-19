package com.example.gifserverv2.domain.project.dto.response;

import com.example.gifserverv2.domain.user.entity.AdminRole;

public record TeacherListResponse(
        Long id,
        String name,
        AdminRole adminRole,
        boolean isLoggedIn,
        boolean isScoreSubmitted
) {
}
