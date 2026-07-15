package com.example.gifserverv2.domain.notice.dto.request;

import java.util.List;

public record CreateNoticeRequest(
        String title,
        String content,
        List<Integer> targetGrades,
        List<Long> targetProjectIds
) {}