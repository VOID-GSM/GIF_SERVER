package com.example.gifserverv2.domain.auth.dto.response;

public record OAuthSignInResponse(
        String accessToken,
        Long userId,
        String email,
        String name,
        String studentNumber,
        String grade,
        String role,
        String adminRole,
        String adminTeam,
        boolean gradeHead,
        String clientRole) {
}
