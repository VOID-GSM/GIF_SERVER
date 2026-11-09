package com.example.gifserverv2.domain.retrospective.dto.request;

import com.example.gifserverv2.domain.retrospective.entity.RelatedWorkType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "관련 이슈·PR")
public record RelatedWorkRequest(
        @Schema(description = "유형. ISSUE / PR", example = "PR")
        @NotNull(message = "관련 작업 유형은 필수입니다.")
        RelatedWorkType type,

        @Schema(description = "이슈·PR 번호 (1 이상)", example = "189")
        @NotNull(message = "이슈/PR 번호는 필수입니다.")
        @Positive(message = "이슈/PR 번호는 1 이상이어야 합니다.")
        Integer number,

        @Schema(description = "이슈·PR 제목 (최대 200자)", example = "프로젝트 반응 API 추가")
        @NotBlank(message = "관련 작업 제목은 필수입니다.")
        @Size(max = 200, message = "관련 작업 제목은 200자를 초과할 수 없습니다.")
        String title,

        @Schema(description = "이슈·PR 주소 (최대 500자, 선택)", example = "https://github.com/VOID-GSM/GIF-server-v2/pull/189")
        @Size(max = 500, message = "관련 작업 URL은 500자를 초과할 수 없습니다.")
        String url
) {}
