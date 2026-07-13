package com.example.gifserverv2.domain.inquiry.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AnswerInquiryRequest(
        @NotBlank(message = "답변 내용은 필수입니다.")
        String answerContent
) {
}