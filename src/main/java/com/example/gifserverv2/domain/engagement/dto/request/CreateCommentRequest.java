package com.example.gifserverv2.domain.engagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @NotBlank(message = "댓글 내용은 비어 있을 수 없습니다.")
        @Size(max = 500, message = "댓글은 500자를 초과할 수 없습니다.")
        String content
) {}