package com.example.gifserverv2.domain.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 도움받기 결과")
public record RetrospectiveAiResponse(
        @Schema(description = "결과 본문 마크다운", example = "## 문제\n새로고침할 때마다 조회수가 올라갔다.")
        String content
) {}
