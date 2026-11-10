package com.example.gifserverv2.domain.retrospective.dto.request;

import com.example.gifserverv2.domain.retrospective.entity.RetrospectiveCategory;
import com.example.gifserverv2.domain.retrospective.entity.RetrospectiveVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "활동 기록 작성 요청")
public record CreateRetrospectiveRequest(
        @Schema(description = "카테고리. FEATURE_DEV(기능 개발) / FEATURE_IMPROVEMENT(기능 개선) / REFACTORING(리팩토링) / TROUBLESHOOTING(트러블슈팅) / RETROSPECTIVE(회고) / PORTFOLIO(포트폴리오)", example = "TROUBLESHOOTING")
        @NotNull(message = "카테고리는 필수 입력 항목입니다.")
        RetrospectiveCategory category,

        @Schema(description = "공개 범위. PUBLIC(전체 공개) / PRIVATE(나만 보기)", example = "PUBLIC")
        @NotNull(message = "공개 범위는 필수 입력 항목입니다.")
        RetrospectiveVisibility visibility,

        @Schema(description = "제목 (최대 100자)", example = "Redis로 조회수 중복 막기")
        @NotBlank(message = "제목은 필수 입력 항목입니다.")
        @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
        String title,

        @Schema(description = "본문 마크다운 (최대 20000자)", example = "## 문제\n새로고침마다 조회수가 올라갔다.")
        @NotBlank(message = "내용은 필수 입력 항목입니다.")
        @Size(max = 20000, message = "내용은 20000자를 초과할 수 없습니다.")
        String content,

        @Schema(description = "관련 이슈·PR 목록 (선택)")
        @Valid
        List<RelatedWorkRequest> relatedWorks,

        @Schema(description = "태그 (최대 10개, 각 30자 이하). 앞뒤 공백 제거·중복 제거 후 저장", example = "[\"Redis\", \"Spring\"]")
        @Size(max = 10, message = "태그는 최대 10개까지 등록할 수 있습니다.")
        List<@NotBlank(message = "태그는 비어 있을 수 없습니다.")
        @Size(max = 30, message = "태그는 30자를 초과할 수 없습니다.") String> tags
) {}