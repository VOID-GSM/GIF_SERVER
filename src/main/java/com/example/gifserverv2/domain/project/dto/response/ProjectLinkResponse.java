package com.example.gifserverv2.domain.project.dto.response;

import com.example.gifserverv2.domain.project.entity.ProjectLink;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로젝트 링크")
public record ProjectLinkResponse(
        @Schema(description = "링크 ID", example = "3")
        Long id,
        @Schema(description = "링크 제목", example = "GitHub")
        String title,
        @Schema(description = "링크 주소", example = "https://github.com/VOID-GSM/GIF-server-v2")
        String url
) {
    public static ProjectLinkResponse from(ProjectLink link) {
        return new ProjectLinkResponse(link.getId(), link.getTitle(), link.getUrl());
    }
}