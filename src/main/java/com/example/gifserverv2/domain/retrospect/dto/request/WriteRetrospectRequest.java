package com.example.gifserverv2.domain.retrospect.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WriteRetrospectRequest(
        @NotBlank(message = "제목은 필수 입력 항목입니다.")
        @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
        String title,

        @NotBlank(message = "내용은 필수 입력 항목입니다.")
        @Size(max = 10000, message = "내용은 10000자를 초과할 수 없습니다.")
        String content
) {}