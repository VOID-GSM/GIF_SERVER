package com.example.gifserverv2.domain.project.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "선생님 프로젝트 메모")
public record GetProjectNoteResponse(
        @Schema(description = "프로젝트 ID", example = "1")
        Long projectId,
        @Schema(description = "메모 내용. 작성하지 않았으면 빈 문자열", example = "발표 자료 보완 필요")
        String content
) {
    public static GetProjectNoteResponse of(Long projectId, String content) {
        return new GetProjectNoteResponse(projectId, content);
    }
}
