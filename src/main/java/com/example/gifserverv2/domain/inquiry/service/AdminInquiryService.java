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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Page<ListInquiryResponse> getAllInquiries(Pageable pageable) {
        Pageable sorted = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdAt").descending());
        Page<Inquiry> inquiries = inquiryRepository.findAll(sorted);

        Set<Long> userIds = inquiries.stream()
                .map(Inquiry::getCreatedByUserId)
                .collect(Collectors.toSet());
        Map<Long, String> userNameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, UserEntity::getName));

        return inquiries.map(inquiry -> ListInquiryResponse.from(inquiry, userNameMap.get(inquiry.getCreatedByUserId())));
    }

    public DetailInquiryResponse getInquiryDetail(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(InquiryException::notFound);

        UserEntity user = userRepository.findById(inquiry.getCreatedByUserId()).orElse(null);
        String createdByName = user != null ? user.getName() : null;

        return DetailInquiryResponse.from(inquiry, createdByName);
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