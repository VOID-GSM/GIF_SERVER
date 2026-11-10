package com.example.gifserverv2.domain.inquiry.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "문의 작성 요청 (multipart/form-data)")
public record CreateInquiryRequest(
        @Schema(description = "제목 (최대 100자)", example = "로그인이 안 돼요")
        @NotBlank(message = "제목은 필수 입력 항목입니다.")
        @Size(max = 100, message = "제목은 최대 100자까지 입력 가능합니다.")
        String title,

        @Schema(description = "내용 (최대 1000자)", example = "DataGSM 로그인 후 빈 화면이 나옵니다.")
        @NotBlank(message = "내용은 필수 입력 항목입니다.")
        @Size(max = 1000, message = "내용은 최대 1000자까지 입력 가능합니다.")
        String content,

        @Schema(description = "첨부 파일 (선택, 최대 10MB)", type = "string", format = "binary")
        MultipartFile file
) {}