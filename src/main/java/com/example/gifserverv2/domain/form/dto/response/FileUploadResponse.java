package com.example.gifserverv2.domain.form.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "파일 업로드 결과")
public record FileUploadResponse(
        @Schema(description = "저장된 파일 URL")
        String filePath,
        @Schema(description = "원본 파일명", example = "보고서.pdf")
        String originalFileName
) {}

