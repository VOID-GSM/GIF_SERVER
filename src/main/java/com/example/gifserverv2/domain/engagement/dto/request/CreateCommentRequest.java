package com.example.gifserverv2.domain.engagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "댓글 작성 요청")
public record CreateCommentRequest(
        @Schema(description = "댓글 내용 (공백 불가, 최대 500자). 앞뒤 공백은 제거되어 저장된다", example = "멋진 프로젝트네요!")
        @NotBlank(message = "댓글 내용은 비어 있을 수 없습니다.")
        @Size(max = 500, message = "댓글은 500자를 초과할 수 없습니다.")
        String content
) {}