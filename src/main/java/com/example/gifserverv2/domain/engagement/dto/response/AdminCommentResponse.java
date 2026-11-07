package com.example.gifserverv2.domain.engagement.dto.response;

import com.example.gifserverv2.domain.engagement.entity.ProjectComment;

import java.time.Instant;

public record AdminCommentResponse(Long id, Long projectId, Long userId, String authorName,
                                   String content, Instant createdAt, boolean hidden) {

    public static AdminCommentResponse from(ProjectComment c) {
        return new AdminCommentResponse(c.getId(), c.getProjectId(), c.getUserId(), c.getAuthorName(),
                c.getContent(), c.getCreatedAt(), c.isHidden());
    }
}