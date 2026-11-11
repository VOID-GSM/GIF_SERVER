package com.example.gifserverv2.domain.engagement.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "좋아요한 프로젝트 목록 페이지")
public record MyLikedProjectPageResponse(
        @Schema(description = "프로젝트 목록 (좋아요 누른 최신순)")
        List<MyLikedProjectResponse> content,
        @Schema(description = "전체 개수", example = "7")
        long totalElements,
        @Schema(description = "다음 페이지 존재 여부", example = "false")
        boolean hasNext
) {

    public static MyLikedProjectPageResponse from(Page<MyLikedProjectResponse> page) {
        return new MyLikedProjectPageResponse(page.getContent(), page.getTotalElements(), page.hasNext());
    }
}
