package com.example.gifserverv2.domain.inquiry.service;

import com.example.gifserverv2.domain.inquiry.dto.response.DetailInquiryResponse;
import com.example.gifserverv2.domain.inquiry.dto.response.ListInquiryResponse;
import com.example.gifserverv2.domain.inquiry.entity.Inquiry;
import com.example.gifserverv2.global.exception.InquiryException;
import com.example.gifserverv2.domain.inquiry.repository.InquiryRepository;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import com.example.gifserverv2.global.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientInquiryService {

    private final InquiryWriter inquiryWriter;
    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;
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

    @Transactional
    protected Long saveInquiry(Long userId, String title, String content,
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


    public Page<ListInquiryResponse> getMyInquiries(Long userId, Pageable pageable) {
        return inquiryRepository.findAllByCreatedByUserId(userId, pageable)
                .map(inquiry -> ListInquiryResponse.from(inquiry, null));
    }

    public DetailInquiryResponse getMyInquiryDetail(Long userId, Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(InquiryException::notFound);

        if (!inquiry.getCreatedByUserId().equals(userId)) {
            throw InquiryException.forbidden();
        }

        UserEntity user = userRepository.findById(userId).orElse(null);
        String createdByName = user != null ? user.getName() : null;

        return DetailInquiryResponse.from(inquiry, createdByName);
    }
}