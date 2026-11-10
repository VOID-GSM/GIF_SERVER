package com.example.gifserverv2.domain.engagement.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "댓글 목록 페이지")
public record CommentPageResponse(
        @Schema(description = "댓글 목록 (최신순, 숨김·삭제 제외)")
        List<CommentResponse> content,
        @Schema(description = "전체 댓글 수", example = "42")
        long totalElements,
        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {

    public static CommentPageResponse from(Page<CommentResponse> page) {
        return new CommentPageResponse(page.getContent(), page.getTotalElements(), page.hasNext());
    }
}