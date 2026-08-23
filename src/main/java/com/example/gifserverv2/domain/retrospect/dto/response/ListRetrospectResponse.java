package com.example.gifserverv2.domain.retrospect.dto.response;

import com.example.gifserverv2.domain.retrospect.entity.Retrospect;

import java.time.LocalDateTime;

public record ListRetrospectResponse(
        Long id,
        Long userId,
        String userName,
        String title,
        LocalDateTime updatedAt
) {
    public static ListRetrospectResponse from(Retrospect retrospect, String userName) {
        return new ListRetrospectResponse(
                retrospect.getId(),
                retrospect.getUserId(),
                userName,
                retrospect.getTitle(),
                retrospect.getUpdatedAt()
        );
    }
}