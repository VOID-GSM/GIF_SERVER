package com.example.gifserverv2.domain.engagement.dto.response;

import com.example.gifserverv2.domain.engagement.entity.ProjectComment;

import java.time.Instant;

public record CommentResponse(Long id, Author author, String content, Instant createdAt) {

    public record Author(Long id, String name) {}

    public static CommentResponse from(ProjectComment c) {
        return new CommentResponse(c.getId(), new Author(c.getUserId(), c.getAuthorName()), c.getContent(), c.getCreatedAt());
    }
}