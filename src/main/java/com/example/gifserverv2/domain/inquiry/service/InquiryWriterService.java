package com.example.gifserverv2.domain.inquiry.service;

import com.example.gifserverv2.domain.inquiry.entity.Inquiry;
import com.example.gifserverv2.domain.inquiry.repository.InquiryRepository;
import com.example.gifserverv2.global.exception.InquiryException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InquiryWriterService {

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

    @Transactional
    public String update(Long userId, Long inquiryId, String title, String content,
                         String newFilePath, String originalFileName, Long fileSize,
                         boolean fileReplaced) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(InquiryException::notFound);

        if (!inquiry.getCreatedByUserId().equals(userId)) {
            throw InquiryException.forbidden();
        }

        if (inquiry.getStatus() == com.example.gifserverv2.domain.inquiry.entity.InquiryStatus.ANSWERED) {
            throw InquiryException.alreadyAnswered();
        }

        String oldFilePath = inquiry.getFilePath();

        if (fileReplaced) {
            inquiry.update(title, content, newFilePath, originalFileName, fileSize);
        } else {
            inquiry.update(title, content, inquiry.getFilePath(), inquiry.getOriginalFileName(), inquiry.getFileSize());
        }

        return fileReplaced ? oldFilePath : null;
    }
}