package com.example.gifserverv2.domain.inquiry.service;

import com.example.gifserverv2.domain.inquiry.dto.response.DetailInquiryResponse;
import com.example.gifserverv2.domain.inquiry.dto.response.ListInquiryResponse;
import com.example.gifserverv2.domain.inquiry.entity.Inquiry;
import com.example.gifserverv2.domain.inquiry.repository.InquiryRepository;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import com.example.gifserverv2.global.exception.InquiryException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.admin-email}")
    private String masterEmail;

    public List<ListInquiryResponse> getAllInquiries(String email) {
        validateMaster(email);

        List<Inquiry> inquiries = inquiryRepository.findAllByOrderByCreatedAtDesc();

        Set<Long> userIds = inquiries.stream()
                .map(Inquiry::getCreatedByUserId)
                .collect(Collectors.toSet());
        Map<Long, String> userNameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, UserEntity::getName));

        return inquiries.stream()
                .map(inquiry -> ListInquiryResponse.from(inquiry, userNameMap.get(inquiry.getCreatedByUserId())))
                .toList();
    }

    public DetailInquiryResponse getInquiryDetail(String email, Long inquiryId) {
        validateMaster(email);

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(InquiryException::notFound);

        UserEntity user = userRepository.findById(inquiry.getCreatedByUserId()).orElse(null);
        String createdByName = user != null ? user.getName() : null;

        return DetailInquiryResponse.from(inquiry, createdByName);
    }

    @Transactional
    public void answerInquiry(String email, Long inquiryId, String answerContent) {
        validateMaster(email);

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(InquiryException::notFound);
        inquiry.answer(answerContent);
    }

    private void validateMaster(String email) {
        if (!masterEmail.equalsIgnoreCase(email)) {
            throw InquiryException.notMaster();
        }
    }
}