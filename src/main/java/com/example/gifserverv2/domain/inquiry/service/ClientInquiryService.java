package com.example.gifserverv2.domain.inquiry.service;

import com.example.gifserverv2.domain.inquiry.dto.response.DetailInquiryResponse;
import com.example.gifserverv2.domain.inquiry.dto.response.ListInquiryResponse;
import com.example.gifserverv2.domain.inquiry.entity.Inquiry;
import com.example.gifserverv2.domain.inquiry.entity.InquiryStatus;
import com.example.gifserverv2.domain.inquiry.repository.InquiryRepository;
import com.example.gifserverv2.global.exception.InquiryException;
import com.example.gifserverv2.global.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientInquiryService {

    private final InquiryWriterService inquiryWriter;
    private final FileStorageService fileStorageService;
    private final InquiryRepository inquiryRepository;

    private static final String INQUIRY_DIRECTORY = "inquiry";

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
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

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void updateInquiry(Long userId, Long inquiryId, String title, String content, MultipartFile file) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(InquiryException::notFound);

        if (!inquiry.getCreatedByUserId().equals(userId)) {
            throw InquiryException.forbidden();
        }
        if (inquiry.getStatus() == InquiryStatus.ANSWERED) {
            throw InquiryException.alreadyAnswered();
        }

        boolean fileReplaced = file != null && !file.isEmpty();
        String savedPath = null;
        String originalFileName = null;
        Long fileSize = null;

        if (fileReplaced) {
            savedPath = fileStorageService.save(file, INQUIRY_DIRECTORY);
            originalFileName = file.getOriginalFilename();
            fileSize = file.getSize();
        }

        try {
            String oldFilePath = inquiryWriter.update(
                    userId, inquiryId, title, content, savedPath, originalFileName, fileSize, fileReplaced
            );
            if (oldFilePath != null) {
                fileStorageService.delete(oldFilePath);
            }
        } catch (RuntimeException e) {
            if (savedPath != null) {
                fileStorageService.delete(savedPath);
            }
            throw e;
        }
    }

    public List<ListInquiryResponse> getMyInquiries(Long userId, String username) {
        return inquiryRepository.findAllByCreatedByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(inquiry -> ListInquiryResponse.from(inquiry, username))
                .toList();
    }

    public DetailInquiryResponse getMyInquiryDetail(Long userId, String username, Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(InquiryException::notFound);
        if (!inquiry.getCreatedByUserId().equals(userId)) {
            throw InquiryException.forbidden();
        }
        return DetailInquiryResponse.from(inquiry, username);
    }
}