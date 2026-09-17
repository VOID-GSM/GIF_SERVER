package com.example.gifserverv2.domain.retrospective.dto.request;

import com.example.gifserverv2.domain.retrospective.entity.RelatedWorkType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RelatedWorkRequest(
        @NotNull(message = "관련 작업 유형은 필수입니다.")
        RelatedWorkType type,

        @NotNull(message = "이슈/PR 번호는 필수입니다.")
        @Positive(message = "이슈/PR 번호는 1 이상이어야 합니다.")
        Integer number,

        @NotBlank(message = "관련 작업 제목은 필수입니다.")
        @Size(max = 200, message = "관련 작업 제목은 200자를 초과할 수 없습니다.")
        String title
) {}
