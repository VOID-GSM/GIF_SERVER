package com.example.gifserverv2.domain.push.event;

import java.util.List;

public record ScorePublishedEvent(
        List<Long> userIds,
        String title,
        String body
) {}
