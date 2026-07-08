package com.example.gifserverv2.domain.project.dto.response;

import com.example.gifserverv2.domain.user.entity.UserEntity;

public record UserSearchResponse(
        Long userId,
        String name,
        String studentNumber,
        boolean hasTeam
) {
    public static UserSearchResponse from(UserEntity user, boolean hasTeam) {
        return new UserSearchResponse(
                user.getId(),
                user.getName(),
                user.getStudentNumber(),
                hasTeam
        );
    }
}