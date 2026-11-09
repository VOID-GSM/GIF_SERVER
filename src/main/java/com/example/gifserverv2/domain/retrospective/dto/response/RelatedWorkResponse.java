package com.example.gifserverv2.domain.retrospective.dto.response;

import com.example.gifserverv2.domain.retrospective.entity.RelatedWorkType;
import com.example.gifserverv2.domain.retrospective.entity.RetrospectiveRelatedWork;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관련 이슈·PR")
public record RelatedWorkResponse(
        @Schema(description = "유형. ISSUE / PR", example = "PR")
        RelatedWorkType type,
        @Schema(description = "이슈·PR 번호", example = "189")
        Integer number,
        @Schema(description = "이슈·PR 제목", example = "프로젝트 반응 API 추가")
        String title,
        @Schema(description = "이슈·PR 주소. 없으면 null", example = "https://github.com/VOID-GSM/GIF-server-v2/pull/189")
        String url
) {
    public static RelatedWorkResponse from(RetrospectiveRelatedWork work) {
        return new RelatedWorkResponse(work.getType(), work.getNumber(), work.getTitle(), work.getUrl());
    }
}
