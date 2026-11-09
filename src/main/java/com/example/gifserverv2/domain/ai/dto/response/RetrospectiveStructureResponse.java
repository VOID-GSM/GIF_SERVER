package com.example.gifserverv2.domain.ai.dto.response;

import java.util.List;

/**
 * @param sections 추천 목차
 * @param content  목차에 맞춰 본문을 재배치한 마크다운 초안. 본문이 비어 있었으면 목차와 작성 안내만 담긴다.
 */
public record RetrospectiveStructureResponse(
        List<String> sections,
        String content
) {}
