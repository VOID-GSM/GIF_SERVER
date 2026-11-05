package com.example.gifserverv2.domain.push.service;

import com.example.gifserverv2.domain.push.entity.PushMessageTemplate;
import com.example.gifserverv2.domain.push.repository.NotificationRepository;
import com.example.gifserverv2.domain.push.repository.PushSubscriptionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.martijndwars.webpush.PushService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PushSenderServicePayloadTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PushSenderService senderService = new PushSenderService(mock(PushSubscriptionRepository.class),
            mock(NotificationRepository.class), objectMapper, mock(PushService.class));

    @Test
    void payload_에_tag_가_들어간다() throws Exception {
        JsonNode payload = objectMapper.readTree(
                senderService.createPayload("제목", "본문", "/form/12/submit", "form-announced-12"));

        assertThat(payload.get("tag").asText()).isEqualTo("form-announced-12");
        assertThat(payload.get("url").asText()).isEqualTo("/form/12/submit");
    }

    @Test
    void tag_가_없으면_payload_에_넣지_않는다() throws Exception {
        JsonNode payload = objectMapper.readTree(senderService.createPayload("제목", "본문", null, null));

        assertThat(payload.has("tag")).isFalse();
    }

    @Test
    void tag_는_알림_종류와_대상_id_로_만든다() {
        assertThat(PushMessageTemplate.DEADLINE_5_HOURS.formatTag(12L)).isEqualTo("deadline-5-hours-12");
        assertThat(PushMessageTemplate.INQUIRY_ANSWERED.formatTag(34L)).isEqualTo("inquiry-answered-34");
        assertThat(PushMessageTemplate.SCORE_PUBLISHED.formatTag()).isEqualTo("score-published");
    }
}
