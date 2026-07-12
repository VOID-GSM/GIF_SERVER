package com.example.gifserverv2.domain.inquiry.service;

import com.example.gifserverv2.global.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientInquiryService {

    private final InquiryWriter inquiryWriter;
    private final FileStorageService fileStorageService;

    private static final String INQUIRY_DIRECTORY = "inquiry";

    public Long createInquiry(Long userId, String title, String content, MultipartFile file) {
        String savedPath = null;
        String originalFileName = null;
        Long fileSize = null;

        if (file != null && !file.isEmpty()) {
            savedPath = fileStorageService.save(file, INQUIRY_DIRECTORY);
            originalFileName = file.getOriginalFilename();
            fileSize = file.getSize();
        }

        try {
            return inquiryWriter.save(userId, title, content, savedPath, originalFileName, fileSize);
        } catch (RuntimeException e) {
            if (savedPath != null) {
                fileStorageService.delete(savedPath);
            }
            throw e;
        }
    }
}