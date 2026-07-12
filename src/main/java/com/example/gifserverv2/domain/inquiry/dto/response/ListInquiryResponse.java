package com.example.gifserverv2.domain.inquiry.dto.response;

import com.example.gifserverv2.domain.inquiry.entity.Inquiry;
import com.example.gifserverv2.domain.inquiry.entity.InquiryStatus;

import java.time.LocalDateTime;

public record ListInquiryResponse(
        Long id,
        String title,
        InquiryStatus status,
        String createdByName,
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