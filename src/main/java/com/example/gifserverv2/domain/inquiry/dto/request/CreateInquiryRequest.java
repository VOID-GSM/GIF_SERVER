package com.example.gifserverv2.domain.inquiry.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record CreateInquiryRequest(
        @NotBlank(message = "제목은 필수 입력 항목입니다.")
        @Size(max = 100, message = "제목은 최대 100자까지 입력 가능합니다.")
        String title,

        @NotBlank(message = "내용은 필수 입력 항목입니다.")
        @Size(max = 1000, message = "내용은 최대 1000자까지 입력 가능합니다.")
        String content,

        MultipartFile file
) {}