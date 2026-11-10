package com.example.gifserverv2.domain.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "선생님 프로젝트 메모 저장 요청")
public record CreateProjectNoteRequest(
        @Schema(description = "메모 내용 (최대 1000자)", example = "발표 자료 보완 필요")
        @NotBlank(message = "메모 내용을 입력해 주세요.")
        @Size(max = 1000, message = "메모는 최대 1000자까지 입력 가능합니다.")
        String content
) {}