package com.example.gifserverv2.domain.inquiry.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnswerInquiryRequest(
        @NotBlank(message = "답변 내용은 필수 입력 항목입니다.")
        @Size(max = 1000, message = "답변 내용은 최대 1000자까지 입력 가능합니다.")
        String answerContent
) {
}