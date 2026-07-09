package com.example.gifserverv2.domain.auth.dto.response;

public record CurrentUserResponse(
        Long userId,
        String email,
        String name,
        String studentNumber,
        String grade,
        String role,
        String adminRole,
        String adminTeam,
        boolean gradeHead,
        String clientRole,
        Long projectId,
        String clientTeam) {
}
