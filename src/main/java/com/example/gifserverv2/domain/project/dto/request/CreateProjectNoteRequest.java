package com.example.gifserverv2.domain.project.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateProjectNoteRequest(
        @NotBlank(message = "메모 내용을 입력해 주세요.")
        String content
) {}