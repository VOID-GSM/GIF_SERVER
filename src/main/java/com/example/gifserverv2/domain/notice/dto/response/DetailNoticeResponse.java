package com.example.gifserverv2.domain.notice.dto.response;

import com.example.gifserverv2.domain.notice.entity.Notice;

import java.time.LocalDateTime;
import java.util.List;

public record DetailNoticeResponse(
        Long id,
        String title,
        String content,
        List<Integer> targetGrades,
        List<String> targetTeamNames,
        LocalDateTime createdAt
) {
    public static DetailNoticeResponse from(Notice notice, List<String> targetTeamNames) {
        return new DetailNoticeResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getTargetGrades(),
                targetTeamNames,
                notice.getCreatedAt()
        );
    }
}