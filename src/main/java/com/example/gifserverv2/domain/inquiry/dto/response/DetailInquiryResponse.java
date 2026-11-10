package com.example.gifserverv2.domain.inquiry.dto.response;

import com.example.gifserverv2.domain.inquiry.entity.Inquiry;
import com.example.gifserverv2.domain.inquiry.entity.InquiryStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "문의 상세")
public record DetailInquiryResponse(
        @Schema(description = "문의 ID", example = "3")
        Long id,
        @Schema(description = "제목", example = "로그인이 안 돼요")
        String title,
        @Schema(description = "내용", example = "DataGSM 로그인 후 빈 화면이 나옵니다.")
        String content,
        @Schema(description = "첨부 파일 URL. 없으면 null")
        String filePath,
        @Schema(description = "첨부 파일 원본 이름")
        String originalFileName,
        @Schema(description = "첨부 파일 크기 (byte)")
        Long fileSize,
        @Schema(description = "상태. PENDING(답변 대기) / ANSWERED(답변 완료)", example = "ANSWERED")
        InquiryStatus status,
        @Schema(description = "답변 내용. 답변 전이면 null", example = "확인 후 수정했습니다.")
        String answerContent,
        @Schema(description = "답변 일시. 답변 전이면 null", example = "2026-11-10T09:00:00")
        LocalDateTime answeredAt,
        @Schema(description = "작성자 이름", example = "홍길동")
        String createdByName,
        @Schema(description = "작성 일시", example = "2026-11-09T10:00:00")
        LocalDateTime createdAt
) {
    public static DetailInquiryResponse from(Inquiry inquiry, String createdByName) {
        return new DetailInquiryResponse(
                inquiry.getId(),
                inquiry.getTitle(),
                inquiry.getContent(),
                inquiry.getFilePath(),
                inquiry.getOriginalFileName(),
                inquiry.getFileSize(),
                inquiry.getStatus(),
                inquiry.getAnswerContent(),
                inquiry.getAnsweredAt(),
                createdByName,
                inquiry.getCreatedAt()
        );
    }
}