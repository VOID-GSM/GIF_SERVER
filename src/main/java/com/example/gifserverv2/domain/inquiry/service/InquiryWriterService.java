package com.example.gifserverv2.domain.inquiry.service;

import com.example.gifserverv2.domain.inquiry.entity.Inquiry;
import com.example.gifserverv2.domain.inquiry.entity.InquiryStatus;
import com.example.gifserverv2.domain.inquiry.repository.InquiryRepository;
import com.example.gifserverv2.domain.push.service.PushSenderService;
import com.example.gifserverv2.domain.user.entity.AdminRole;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import com.example.gifserverv2.global.exception.InquiryException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.gifserverv2.domain.push.entity.PushMessageTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryWriterService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;
    private final PushSenderService pushSenderService;

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

        Long savedInquiryId = inquiryRepository.save(inquiry).getId();

        List<Long> voidUserIds = userRepository.findUserIdsByAdminRole(AdminRole.VOID);

        if (!voidUserIds.isEmpty()) {
            pushSenderService.sendBulkNotifications(
                    voidUserIds,
                    PushMessageTemplate.INQUIRY_RECEIVED.getTitle(),
                    PushMessageTemplate.INQUIRY_RECEIVED.getBodyTemplate()
            );
        }

        return savedInquiryId;
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

        if (inquiry.getStatus() == InquiryStatus.ANSWERED) {
            throw InquiryException.alreadyAnswered();
        }

        inquiry.update(title, content);

        if (fileReplaced) {
            String oldFilePath = inquiry.getFilePath();
            inquiry.updateFile(newFilePath, originalFileName, fileSize);
            return oldFilePath;
        }
        return null;
    }
}