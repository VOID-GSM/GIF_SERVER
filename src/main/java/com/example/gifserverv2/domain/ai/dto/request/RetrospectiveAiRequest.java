package com.example.gifserverv2.domain.ai.dto.request;

import com.example.gifserverv2.domain.retrospective.entity.RetrospectiveCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 회고록 AI 도움받기 공통 요청. 저장하지 않은 글에도 쓸 수 있도록 회고 id 대신 본문을 받는다.
 */
public record RetrospectiveAiRequest(
        @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
        String title,

        @NotNull(message = "카테고리는 필수 입력 항목입니다.")
        RetrospectiveCategory category,

        @Size(max = RetrospectiveAiRequest.MAX_CONTENT_LENGTH,
                message = "AI 도움받기는 본문 " + RetrospectiveAiRequest.MAX_CONTENT_LENGTH + "자까지 사용할 수 있습니다.")
        String content
) {
    /** 응답 시간과 비용 때문에 회고 작성 한도(20000자)보다 작게 둔다. */
    public static final int MAX_CONTENT_LENGTH = 5000;
}
