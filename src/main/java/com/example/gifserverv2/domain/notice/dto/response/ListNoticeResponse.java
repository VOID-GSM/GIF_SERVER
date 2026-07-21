package com.example.gifserverv2.domain.notice.dto.response;

import com.example.gifserverv2.domain.notice.entity.Notice;

import java.time.LocalDateTime;
import java.util.List;

public record ListNoticeResponse(
        Long id,
        String title,
        List<Integer> targetGrades,
        List<String> targetTeamNames,
        LocalDateTime createdAt
) {
    public static ListNoticeResponse from(Notice notice, List<String> targetTeamNames) {
        return new ListNoticeResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getTargetGrades(),
                targetTeamNames,
                notice.getCreatedAt()
        );
    }
}