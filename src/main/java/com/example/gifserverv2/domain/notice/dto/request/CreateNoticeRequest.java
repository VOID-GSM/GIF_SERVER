package com.example.gifserverv2.domain.notice.dto.request;

import java.util.List;

public record CreateNoticeRequest(
        @jakarta.validation.constraints.NotBlank(message = "제목은 필수 입력 항목입니다.")
        @jakarta.validation.constraints.Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
        String title,
        @jakarta.validation.constraints.NotBlank(message = "내용은 필수 입력 항목입니다.")
        @jakarta.validation.constraints.Size(max = 2000, message = "내용은 2000자를 초과할 수 없습니다.")
        String content,
        List<Integer> targetGrades,
        List<Long> targetProjectIds
) {}