package com.example.gifserverv2.domain.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectNoteRequest(
        @NotBlank(message = "메모 내용을 입력해 주세요.")
        @Size(max = 1000, message = "메모는 최대 1000자까지 입력 가능합니다.")
        String content
) {}