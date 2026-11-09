package com.example.gifserverv2.domain.form.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "마감 준수 여부 지정 요청")
public record DeadlineComplianceRequest(
        @Schema(description = "마감 준수 여부. null이면 지정을 해제하고 자동 계산", example = "true")
        Boolean deadlineComplied
) {}