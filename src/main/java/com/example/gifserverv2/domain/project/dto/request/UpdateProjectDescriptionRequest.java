package com.example.gifserverv2.domain.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "프로젝트 설명 수정 요청")
public record UpdateProjectDescriptionRequest(
        @Schema(description = "새 설명 (최대 500자). null이면 변경하지 않는다", example = "수정된 설명")
        @Size(max = 500, message = "프로젝트 설명은 500자 이하여야 합니다.")
        String description
) {}