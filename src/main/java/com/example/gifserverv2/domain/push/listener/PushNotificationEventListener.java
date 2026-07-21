package com.example.gifserverv2.domain.push.listener;

import com.example.gifserverv2.domain.push.event.ScorePublishedEvent;
import com.example.gifserverv2.domain.push.service.PushSenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PushNotificationEventListener {

    private final PushSenderService pushSenderService;

    @Async
    @EventListener
    public void handleScorePublishedEvent(ScorePublishedEvent event) {
        pushSenderService.sendBulkNotifications(
                event.userIds(),
                event.title(),
                event.body()
        );
    }
}
