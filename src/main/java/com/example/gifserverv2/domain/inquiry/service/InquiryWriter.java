package com.example.gifserverv2.domain.inquiry.service;

import com.example.gifserverv2.domain.inquiry.entity.Inquiry;
import com.example.gifserverv2.domain.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InquiryWriter {

    private final InquiryRepository inquiryRepository;

    @Transactional
    public Long save(Long userId, String title, String content,
                     String filePath, String originalFileName, Long fileSize) {
        Inquiry inquiry = Inquiry.builder()
                .title(title)
                .content(content)
                .createdByUserId(userId)
                .filePath(filePath)
                .originalFileName(originalFileName)
                .fileSize(fileSize)
                .build();

        return inquiryRepository.save(inquiry).getId();
    }
}