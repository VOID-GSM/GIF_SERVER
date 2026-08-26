package com.example.gifserverv2.domain.retrospect.dto.response;

import com.example.gifserverv2.domain.retrospect.entity.Retrospect;

import java.time.LocalDateTime;

public record DetailRetrospectResponse(
        Long id,
        Long userId,
        String userName,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static DetailRetrospectResponse from(Retrospect retrospect, String userName) {
        return new DetailRetrospectResponse(
                retrospect.getId(),
                retrospect.getUserId(),
                userName,
                retrospect.getTitle(),
                retrospect.getContent(),
                retrospect.getCreatedAt(),
                retrospect.getUpdatedAt()
        );
    }
}