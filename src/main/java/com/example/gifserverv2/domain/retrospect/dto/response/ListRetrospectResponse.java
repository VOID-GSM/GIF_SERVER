package com.example.gifserverv2.domain.retrospect.dto.response;

import com.example.gifserverv2.domain.retrospect.entity.Retrospect;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "프로젝트 회고록 목록 항목")
public record ListRetrospectResponse(
        @Schema(description = "회고록 ID", example = "4")
        Long id,
        @Schema(description = "작성자 userId", example = "12")
        Long userId,
        @Schema(description = "작성자 이름", example = "홍길동")
        String userName,
        @Schema(description = "제목", example = "GIF 프로젝트를 마치며")
        String title,
        @Schema(description = "마지막 수정 일시", example = "2026-11-10T09:00:00")
        LocalDateTime updatedAt
) {
    public static ListRetrospectResponse from(Retrospect retrospect, String userName) {
        return new ListRetrospectResponse(
                retrospect.getId(),
                retrospect.getUserId(),
                userName,
                retrospect.getTitle(),
                retrospect.getUpdatedAt()
        );
    }
}
