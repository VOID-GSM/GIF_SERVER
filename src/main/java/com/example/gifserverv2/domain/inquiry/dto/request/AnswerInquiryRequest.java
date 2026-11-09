package com.example.gifserverv2.domain.inquiry.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "문의 답변 요청")
public record AnswerInquiryRequest(
        @Schema(description = "답변 내용", example = "확인 후 수정했습니다.")
        @NotBlank(message = "답변 내용은 필수입니다.")
        String answerContent
) {
}