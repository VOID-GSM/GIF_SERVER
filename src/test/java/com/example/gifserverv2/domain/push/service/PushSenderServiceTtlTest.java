package com.example.gifserverv2.domain.push.service;

import com.example.gifserverv2.domain.push.entity.PushMessageTemplate;
import com.example.gifserverv2.domain.push.entity.PushOptions;
import com.example.gifserverv2.domain.push.entity.PushSubscription;
import com.example.gifserverv2.domain.push.repository.NotificationRepository;
import com.example.gifserverv2.domain.push.repository.PushSubscriptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Urgency;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.message.BasicHttpResponse;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveGenParameterSpec;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.security.KeyPairGenerator;
import java.security.Security;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PushSenderServiceTtlTest {

    private static String p256dh;
    private static final String AUTH = Base64.getUrlEncoder().withoutPadding().encodeToString(new byte[16]);

    private final PushSubscriptionRepository subscriptionRepository = mock(PushSubscriptionRepository.class);
    private final PushService pushService = mock(PushService.class);
    private PushSenderService senderService;

    @BeforeAll
    static void createSubscriptionKey() throws Exception {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        KeyPairGenerator generator = KeyPairGenerator.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME);
        generator.initialize(new ECNamedCurveGenParameterSpec("prime256v1"));
        ECPublicKey publicKey = (ECPublicKey) generator.generateKeyPair().getPublic();
        p256dh = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getQ().getEncoded(false));
    }

    @BeforeEach
    void setUp() throws Exception {
        senderService = new PushSenderService(subscriptionRepository, mock(NotificationRepository.class),
                new ObjectMapper(), pushService);

        PushSubscription subscription = mock(PushSubscription.class);
        given(subscription.getEndpoint()).willReturn("https://fcm.googleapis.com/fcm/send/abc");
        given(subscription.getP256dh()).willReturn(p256dh);
        given(subscription.getAuth()).willReturn(AUTH);
        given(subscriptionRepository.findAllByUserIdIn(List.of(1L))).willReturn(List.of(subscription));

        HttpResponse created = new BasicHttpResponse(HttpVersion.HTTP_1_1, 201, "Created");
        given(pushService.send(any(Notification.class))).willReturn(created);
    }

    @Test
    void 마감_알림은_짧은_TTL과_high_urgency로_발송한다() throws Exception {
        PushMessageTemplate template = PushMessageTemplate.DEADLINE_5_HOURS;

        senderService.sendBulkNotifications(List.of(1L), template.getTitle(), "본문", "/form/1/submit",
                template.formatTag(1L), template.getPushOptions().withTopic("form-deadline-1"));

        Notification sent = captureSent();
        assertThat(sent.getTTL()).isEqualTo(4 * 60 * 60);
        assertThat(sent.getUrgency()).isEqualTo(Urgency.HIGH);
        assertThat(sent.getTopic()).isEqualTo("form-deadline-1");
    }

    @Test
    void 옵션_없이_보내면_기존과_같이_28일_TTL에_urgency_헤더_없음() throws Exception {
        senderService.sendBulkNotifications(List.of(1L), "제목", "본문", "/projects/1", "project-created-1");

        Notification sent = captureSent();
        assertThat(sent.getTTL()).isEqualTo(28 * 24 * 60 * 60);
        assertThat(sent.hasUrgency()).isFalse();
        assertThat(sent.hasTopic()).isFalse();
    }

    private Notification captureSent() throws Exception {
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(pushService).send(captor.capture());
        return captor.getValue();
    }
}
