package com.example.gifserverv2.domain.inquiry.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "문의 수정 요청 (multipart/form-data)")
public record UpdateInquiryRequest(
        @Schema(description = "제목", example = "로그인이 안 돼요")
        String title,
        @Schema(description = "내용", example = "DataGSM 로그인 후 빈 화면이 나옵니다.")
        String content,
        @Schema(description = "새 첨부 파일 (선택). 보내면 기존 파일을 교체", type = "string", format = "binary")
        MultipartFile file
) {}