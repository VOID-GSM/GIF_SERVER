package com.example.gifserverv2.domain.engagement.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record AdminCommentPageResponse(List<AdminCommentResponse> content, long totalElements, boolean hasNext) {

    public static AdminCommentPageResponse from(Page<AdminCommentResponse> page) {
        return new AdminCommentPageResponse(page.getContent(), page.getTotalElements(), page.hasNext());
    }
}