package com.example.gifserverv2.domain.auth.dto.response;

public record CurrentUserResponse(
        Long id,
        String email,
        String name,
        String studentNumber,
        String grade,
        String role,
        String adminRole,
        String adminTeam,
        boolean isGradeHead,
        String clientRole,
        Long projectId,
        String clientTeam,
        String githubUsername,
        String githubAvatarUrl,
        int coinBalance,
        String accessToken
) {
}