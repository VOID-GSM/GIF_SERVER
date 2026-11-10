package com.example.gifserverv2.domain.retrospective.dto.response;

import com.example.gifserverv2.domain.retrospective.entity.Retrospective;
import com.example.gifserverv2.domain.retrospective.entity.RetrospectiveCategory;
import com.example.gifserverv2.domain.retrospective.entity.RetrospectiveVisibility;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/** 상세 조회 및 임시저장 목록(GET /draft)에서 공통으로 사용 */
@Schema(description = "활동 기록 상세 (임시 저장 목록에서도 사용)")
public record DetailRetrospectiveResponse(
        @Schema(description = "활동 기록 ID", example = "15")
        Long id,
        @Schema(description = "제목", example = "Redis로 조회수 중복 막기")
        String title,
        @Schema(description = "본문 요약 (마크다운 제거, 최대 150자)", example = "조회수가 새로고침마다 올라가는 문제를...")
        String summary,
        @Schema(description = "카테고리. FEATURE_DEV(기능 개발) / FEATURE_IMPROVEMENT(기능 개선) / REFACTORING(리팩토링) / TROUBLESHOOTING(트러블슈팅) / RETROSPECTIVE(회고) / PORTFOLIO(포트폴리오)", example = "TROUBLESHOOTING")
        RetrospectiveCategory category,
        @Schema(description = "공개 범위. PUBLIC(전체 공개) / PRIVATE(나만 보기)", example = "PUBLIC")
        RetrospectiveVisibility visibility,
        @Schema(description = "작성자 소속 프로젝트 학년. 소속이 없으면 null", example = "2")
        Integer grade,
        @Schema(description = "작성자 소속 프로젝트 반", example = "1")
        Integer classNo,
        @Schema(description = "작성자 소속 프로젝트 팀 번호", example = "3")
        Integer teamNo,
        @Schema(description = "작성자의 팀 내 파트", example = "백엔드")
        String part,
        @Schema(description = "북마크 수", example = "3")
        long bookmarkCount,
        @Schema(description = "내가 북마크했는지", example = "false")
        boolean isBookmarked,
        @Schema(description = "작성자 이름", example = "홍길동")
        String authorName,
        @Schema(description = "작성 일시", example = "2026-11-09T10:00:00")
        LocalDateTime createdAt,
        @Schema(description = "본문 마크다운", example = "## 문제\n새로고침마다 조회수가 올라갔다.")
        String content,
        @Schema(description = "관련 이슈·PR 목록")
        List<RelatedWorkResponse> relatedWorks,
        @Schema(description = "임시 저장 기록인지", example = "false")
        boolean isDraft,
        @Schema(description = "내가 쓴 기록인지", example = "true")
        boolean isMine,
        @Schema(description = "태그", example = "[\"Redis\", \"Spring\"]")
        List<String> tags,
        @Schema(description = "작성자 소속 프로젝트 ID", example = "1")
        Long projectId,
        @Schema(description = "작성자 소속 팀 이름", example = "VOID")
        String teamName
) {
    public static DetailRetrospectiveResponse from(
            Retrospective retrospective,
            AuthorAffiliation affiliation,
            String authorName,
            boolean isBookmarked,
            boolean isMine
    ) {
        return new DetailRetrospectiveResponse(
                retrospective.getId(),
                retrospective.getTitle(),
                retrospective.getSummary(),
                retrospective.getCategory(),
                retrospective.getVisibility(),
                affiliation.grade(),
                affiliation.classNo(),
                affiliation.teamNo(),
                affiliation.part(),
                retrospective.getBookmarkCount(),
                isBookmarked,
                authorName,
                retrospective.getCreatedAt(),
                retrospective.getContent(),
                retrospective.getRelatedWorks().stream().map(RelatedWorkResponse::from).toList(),
                retrospective.isDraft(),
                isMine,
                List.copyOf(retrospective.getTags()),
                affiliation.projectId(),
                affiliation.teamName()
        );
    }
}