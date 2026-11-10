package com.example.gifserverv2.domain.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로젝트 링크 수정 요청. 두 값 모두 보낸 값으로 덮어쓴다")
public record UpdateProjectLinkRequest(
        @Schema(description = "링크 제목", example = "GitHub")
        String title,
        @Schema(description = "링크 주소. http:// 또는 https://로 시작해야 한다", example = "https://github.com/VOID-GSM/GIF-server-v2")
        String url
) {}