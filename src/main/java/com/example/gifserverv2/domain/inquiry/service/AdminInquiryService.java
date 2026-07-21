package com.example.gifserverv2.domain.inquiry.service;

import com.example.gifserverv2.domain.inquiry.dto.response.DetailInquiryResponse;
import com.example.gifserverv2.domain.inquiry.dto.response.ListInquiryResponse;
import com.example.gifserverv2.domain.inquiry.entity.Inquiry;
import com.example.gifserverv2.domain.inquiry.repository.InquiryRepository;
import com.example.gifserverv2.domain.push.entity.PushMessageTemplate;
import com.example.gifserverv2.domain.push.service.PushSenderService;
import com.example.gifserverv2.domain.user.entity.AdminRole;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import com.example.gifserverv2.global.exception.InquiryException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
public class AdminInquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;
    private final PushSenderService pushSenderService;

    @Value("${app.admin-email}")
    private String masterEmail;

    @Transactional(readOnly = true)
    public Page<ListInquiryResponse> getAllInquiries(String email, Pageable pageable) {
        validateVoidAdmin(email);

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

    @Transactional(readOnly = true)
    public DetailInquiryResponse getInquiryDetail(String email, Long inquiryId) {
        validateVoidAdmin(email);

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(InquiryException::notFound);

        UserEntity user = userRepository.findById(inquiry.getCreatedByUserId()).orElse(null);
        String createdByName = user != null ? user.getName() : null;

        return DetailInquiryResponse.from(inquiry, createdByName);
    }

    @Transactional
    public void answerInquiry(String email, Long inquiryId, String answerContent) {
        validateVoidAdmin(email);

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(InquiryException::notFound);

        inquiry.answer(answerContent);

        pushSenderService.sendNotification(
                inquiry.getCreatedByUserId(),
                PushMessageTemplate.INQUIRY_ANSWERED.getTitle(),
                PushMessageTemplate.INQUIRY_ANSWERED.getBodyTemplate()
        );
    }

    private void validateVoidAdmin(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(InquiryException::notMaster);

        if (user.getAdminRole() != AdminRole.VOID) {
            throw InquiryException.notMaster();
        }
    }
}