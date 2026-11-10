package com.example.gifserverv2.domain.retrospective.dto.request;

import com.example.gifserverv2.domain.retrospective.entity.RetrospectiveCategory;
import com.example.gifserverv2.domain.retrospective.entity.RetrospectiveVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "활동 기록 수정·임시 저장 요청. 생략한 값은 유지된다")
public record RetrospectivePatchRequest(
        @Schema(description = "카테고리. FEATURE_DEV(기능 개발) / FEATURE_IMPROVEMENT(기능 개선) / REFACTORING(리팩토링) / TROUBLESHOOTING(트러블슈팅) / RETROSPECTIVE(회고) / PORTFOLIO(포트폴리오)", example = "TROUBLESHOOTING")
        RetrospectiveCategory category,
        @Schema(description = "공개 범위. PUBLIC(전체 공개) / PRIVATE(나만 보기)", example = "PUBLIC")
        RetrospectiveVisibility visibility,

        @Schema(description = "제목 (최대 100자)", example = "Redis로 조회수 중복 막기")
        @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
        String title,

        @Schema(description = "본문 마크다운 (최대 20000자)", example = "## 문제\n새로고침마다 조회수가 올라갔다.")
        @Size(max = 20000, message = "내용은 20000자를 초과할 수 없습니다.")
        String content,

        @Schema(description = "관련 이슈·PR 목록. 보내면 전체 교체")
        @Valid
        List<RelatedWorkRequest> relatedWorks,

        @Schema(description = "태그 (최대 10개, 각 30자 이하). 앞뒤 공백 제거·중복 제거 후 저장. 보내면 전체 교체", example = "[\"Redis\", \"Spring\"]")
        @Size(max = 10, message = "태그는 최대 10개까지 등록할 수 있습니다.")
        List<@NotBlank(message = "태그는 비어 있을 수 없습니다.")
        @Size(max = 30, message = "태그는 30자를 초과할 수 없습니다.") String> tags
) {}