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
        boolean hasNext,
        @Schema(description = "다음 요청의 `lastId`로 보낼 커서(이번 목록 마지막 댓글 ID). 다음 페이지가 없으면 null", example = "31", nullable = true)
        Long nextCursor
) {

    public static CommentPageResponse from(Page<CommentResponse> page) {
        return of(page.getContent(), page.getTotalElements(), page.hasNext());
    }

    public static CommentPageResponse of(List<CommentResponse> content, long totalElements, boolean hasNext) {
        Long nextCursor = hasNext && !content.isEmpty() ? content.get(content.size() - 1).id() : null;
        return new CommentPageResponse(content, totalElements, hasNext, nextCursor);
    }
}