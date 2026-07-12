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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminInquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public List<InquiryListResponse> getAllInquiries() {
        List<Inquiry> inquiries = inquiryRepository.findAllByOrderByCreatedAtDesc();

        Set<Long> userIds = inquiries.stream()
                .map(Inquiry::getCreatedByUserId)
                .collect(Collectors.toSet());
        Map<Long, String> userNameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, UserEntity::getName));

        return inquiries.stream()
                .map(inquiry -> InquiryListResponse.from(inquiry, userNameMap.get(inquiry.getCreatedByUserId())))
                .toList();
    }

    public InquiryDetailResponse getInquiryDetail(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(InquiryException::notFound);

        UserEntity user = userRepository.findById(inquiry.getCreatedByUserId()).orElse(null);
        String createdByName = user != null ? user.getName() : null;

        return InquiryDetailResponse.from(inquiry, createdByName);
    }

    @Transactional
    public void answerInquiry(Long inquiryId, String answerContent) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(InquiryException::notFound);
        inquiry.answer(answerContent);
    }

    @Transactional
    public void deleteInquiry(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(InquiryException::notFound);

        if (inquiry.getFilePath() != null) {
            fileStorageService.delete(inquiry.getFilePath());
        }
        inquiryRepository.delete(inquiry);
    }
}