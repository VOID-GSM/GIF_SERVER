package com.example.gifserverv2.domain.auth.dto.response;

public record CurrentUserResponse(
        Long userId,
        String email,
        String name,
        String studentNumber,
        String role,
        String adminRole,
        String clientRole) {
}
