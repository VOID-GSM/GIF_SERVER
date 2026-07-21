package com.example.gifserverv2.domain.notice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateNoticeRequest(
        @NotBlank(message = "제목은 필수 입력 항목입니다.")
        @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
        String title,

        @NotBlank(message = "내용은 필수 입력 항목입니다.")
        @Size(max = 2000, message = "내용은 2000자를 초과할 수 없습니다.")
        String content,

        List<Integer> targetGrades,
        List<Long> targetProjectIds
) {}