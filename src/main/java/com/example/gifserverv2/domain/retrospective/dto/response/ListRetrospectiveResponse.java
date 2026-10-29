package com.example.gifserverv2.domain.retrospective.dto.response;

import com.example.gifserverv2.domain.retrospective.entity.Retrospective;
import com.example.gifserverv2.domain.retrospective.entity.RetrospectiveCategory;
import com.example.gifserverv2.domain.retrospective.entity.RetrospectiveVisibility;

import java.time.LocalDateTime;
import java.util.List;

public record ListRetrospectiveResponse(
        Long id,
        String title,
        String summary,
        RetrospectiveCategory category,
        RetrospectiveVisibility visibility,
        Integer grade,
        Integer classNo,
        Integer teamNo,
        String part,
        long bookmarkCount,
        boolean isBookmarked,
        String authorName,
        LocalDateTime createdAt,
        List<String> tags,
        Long projectId,
        String teamName
) {
    public static ListRetrospectiveResponse from(
            Retrospective retrospective,
            AuthorAffiliation affiliation,
            String authorName,
            boolean isBookmarked
    ) {
        return new ListRetrospectiveResponse(
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
                List.copyOf(retrospective.getTags()),
                affiliation.projectId(),
                affiliation.teamName()
        );
    }
}