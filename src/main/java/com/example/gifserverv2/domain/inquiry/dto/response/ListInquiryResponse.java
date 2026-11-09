package com.example.gifserverv2.domain.inquiry.dto.response;

import com.example.gifserverv2.domain.inquiry.entity.Inquiry;
import com.example.gifserverv2.domain.inquiry.entity.InquiryStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "문의 목록 항목")
public record ListInquiryResponse(
        @Schema(description = "문의 ID", example = "3")
        Long id,
        @Schema(description = "제목", example = "로그인이 안 돼요")
        String title,
        @Schema(description = "상태. PENDING / ANSWERED", example = "PENDING")
        InquiryStatus status,
        @Schema(description = "작성자 이름", example = "홍길동")
        String createdByName,
        @Schema(description = "작성 일시", example = "2026-11-09T10:00:00")
        LocalDateTime createdAt
) {
    public static ListInquiryResponse from(Inquiry inquiry, String createdByName) {
        return new ListInquiryResponse(
                inquiry.getId(),
                inquiry.getTitle(),
                inquiry.getStatus(),
                createdByName,
                inquiry.getCreatedAt()
        );
    }
}