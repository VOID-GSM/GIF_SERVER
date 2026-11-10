package com.example.gifserverv2.domain.ai.dto.request;

import com.example.gifserverv2.domain.retrospective.entity.RetrospectiveCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "AI 도움받기 요청")
public record RetrospectiveAiRequest(
        @Schema(description = "제목 (선택, 최대 100자)", example = "Redis로 조회수 중복 막기")
        @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
        String title,

        @Schema(description = "카테고리. FEATURE_DEV(기능 개발) / FEATURE_IMPROVEMENT(기능 개선) / REFACTORING(리팩토링) / TROUBLESHOOTING(트러블슈팅) / RETROSPECTIVE(회고) / PORTFOLIO(포트폴리오)", example = "TROUBLESHOOTING")
        @NotNull(message = "카테고리는 필수 입력 항목입니다.")
        RetrospectiveCategory category,

        @Size(max = RetrospectiveAiRequest.MAX_CONTENT_LENGTH,
                message = "AI 도움받기는 본문 " + RetrospectiveAiRequest.MAX_CONTENT_LENGTH + "자까지 사용할 수 있습니다.")
        @Schema(description = "본문 마크다운 (최대 5000자). 다듬기·포트폴리오 변환은 20자 이상 필요", example = "## 문제\n새로고침마다 조회수가 올라갔다.")
        String content
) {
    public static final int MAX_CONTENT_LENGTH = 5000;
}
