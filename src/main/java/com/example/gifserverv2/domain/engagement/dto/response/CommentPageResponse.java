package com.example.gifserverv2.domain.engagement.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record CommentPageResponse(List<CommentResponse> content, long totalElements, boolean hasNext) {

    public static CommentPageResponse from(Page<CommentResponse> page) {
        return new CommentPageResponse(page.getContent(), page.getTotalElements(), page.hasNext());
    }
}