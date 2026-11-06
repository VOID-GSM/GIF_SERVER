package com.example.gifserverv2.domain.engagement.dto.response;

import com.example.gifserverv2.domain.engagement.entity.ProjectComment;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record CommentResponse(Long id, Author author, String content, Instant createdAt,
                              @JsonProperty("isMine") boolean isMine) {

    public record Author(Long id, String name, String profileImage) {}

    public static CommentResponse from(ProjectComment c, String profileImage, boolean isMine) {
        return new CommentResponse(
                c.getId(),
                new Author(c.getUserId(), c.getAuthorName(), profileImage),
                c.getContent(),
                c.getCreatedAt(),
                isMine);
    }
}