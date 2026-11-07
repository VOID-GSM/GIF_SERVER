package com.example.gifserverv2.domain.student.service;

import com.example.gifserverv2.domain.push.entity.PushMessageTemplate;
import com.example.gifserverv2.domain.push.service.PushSenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

/**
 * 팀 배정 관련 푸시. 배정이 롤백됐는데 알림만 나가지 않도록 커밋된 뒤에 보낸다.
 */
@Component
@RequiredArgsConstructor
class StudentNotifier {

    private final PushSenderService pushSenderService;

    void send(Long userId, PushMessageTemplate template, String body, String targetUrl, String tag) {
        afterCommit(() -> pushSenderService.sendNotification(userId, template.getTitle(), body, targetUrl, tag));
    }

    void sendAll(List<Long> userIds, PushMessageTemplate template, String body, String targetUrl, String tag) {
        if (userIds.isEmpty()) return;
        afterCommit(() -> pushSenderService.sendBulkNotificationsAsync(userIds, template.getTitle(), body, targetUrl, tag));
    }

    private static void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}
