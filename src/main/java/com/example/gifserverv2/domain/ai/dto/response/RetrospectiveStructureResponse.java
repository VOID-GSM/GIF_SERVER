package com.example.gifserverv2.domain.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "목차 추천 결과")
public record RetrospectiveStructureResponse(
        @Schema(description = "추천 목차 (4~6개)", example = "[\"문제 상황\", \"원인 분석\", \"해결 과정\", \"배운 점\"]")
        List<String> sections,
        @Schema(description = "목차대로 정리한 마크다운 초안. 각 목차는 ## 로 시작", example = "## 문제 상황\n...")
        String content
) {}
