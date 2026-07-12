package com.example.gifserverv2.domain.inquiry.service;

import com.example.gifserverv2.domain.inquiry.dto.response.InquiryDetailResponse;
import com.example.gifserverv2.domain.inquiry.dto.response.InquiryListResponse;
import com.example.gifserverv2.domain.inquiry.entity.Inquiry;
import com.example.gifserverv2.domain.inquiry.exception.InquiryException;
import com.example.gifserverv2.domain.inquiry.repository.InquiryRepository;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import com.example.gifserverv2.global.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientInquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    private static final String INQUIRY_DIRECTORY = "inquiry";

    @Transactional
    public Long createInquiry(Long userId, String title, String content, MultipartFile file) {
        Inquiry.InquiryBuilder builder = Inquiry.builder()
                .title(title)
                .content(content)
                .createdByUserId(userId);

        if (file != null && !file.isEmpty()) {
            String savedPath = fileStorageService.save(file, INQUIRY_DIRECTORY);
            builder.filePath(savedPath)
                    .originalFileName(file.getOriginalFilename())
                    .fileSize(file.getSize());
        }

        Inquiry inquiry = builder.build();
        return inquiryRepository.save(inquiry).getId();
    }

    public List<InquiryListResponse> getMyInquiries(Long userId) {
        return inquiryRepository.findAllByCreatedByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(inquiry -> InquiryListResponse.from(inquiry, null))
                .toList();
    }

    public InquiryDetailResponse getMyInquiryDetail(Long userId, Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(InquiryException::notFound);

        if (!inquiry.getCreatedByUserId().equals(userId)) {
            throw InquiryException.forbidden();
        }

        UserEntity user = userRepository.findById(userId).orElse(null);
        String createdByName = user != null ? user.getName() : null;

        return InquiryDetailResponse.from(inquiry, createdByName);
    }
}