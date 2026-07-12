package com.example.gifserverv2.domain.inquiry.dto.response;

import com.example.gifserverv2.domain.inquiry.entity.Inquiry;
import com.example.gifserverv2.domain.inquiry.entity.InquiryStatus;

import java.time.LocalDateTime;

public record DetailInquiryResponse(
        Long id,
        String title,
        String content,
        String filePath,
        String originalFileName,
        Long fileSize,
        InquiryStatus status,
        String answerContent,
        LocalDateTime answeredAt,
        String createdByName,
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