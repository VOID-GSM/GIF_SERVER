package com.example.gifserverv2.domain.engagement.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "관리자 댓글 목록 페이지")
public record AdminCommentPageResponse(
        @Schema(description = "댓글 목록 (숨김 포함, 삭제 제외)")
        List<AdminCommentResponse> content,
        @Schema(description = "전체 댓글 수", example = "42")
        long totalElements,
        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {

    public static AdminCommentPageResponse from(Page<AdminCommentResponse> page) {
        return new AdminCommentPageResponse(page.getContent(), page.getTotalElements(), page.hasNext());
    }
}