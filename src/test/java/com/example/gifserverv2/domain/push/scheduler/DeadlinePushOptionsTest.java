package com.example.gifserverv2.domain.push.scheduler;

import com.example.gifserverv2.domain.push.entity.PushMessageTemplate;
import com.example.gifserverv2.domain.push.entity.PushOptions;
import com.example.gifserverv2.domain.push.entity.PushOptions.Urgency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 마감 알림 TTL 이 다음 단계 알림 시각을 넘지 않는지 확인한다. (#208)
 */
class DeadlinePushOptionsTest {

    @ParameterizedTest
    @CsvSource({
            "72, DEADLINE_3_DAYS, 48",
            "24, DEADLINE_1_DAY, 19",
            "5, DEADLINE_5_HOURS, 4",
            "1, DEADLINE_1_HOUR, 1"
    })
    void 마감_단계별로_다음_알림_전까지만_유효하다(long hoursLeft, PushMessageTemplate expected, long ttlHours) {
        PushMessageTemplate template = PushScheduler.deadlineTemplate(hoursLeft);
        PushOptions options = template.getPushOptions();

        assertThat(template).isEqualTo(expected);
        assertThat(options.ttl()).isEqualTo(Duration.ofHours(ttlHours));
        assertThat(options.urgency()).isEqualTo(Urgency.HIGH);
    }

    @Test
    void 마감_경과_안내는_24시간만_유효하고_urgency는_기본() {
        PushOptions options = PushScheduler.deadlineTemplate(-1).getPushOptions();

        assertThat(options.ttl()).isEqualTo(Duration.ofHours(24));
        assertThat(options.urgency()).isNull();
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 2, 6, 23, 48, 71, 73})
    void 알림_시점이_아니면_보내지_않는다(long hoursLeft) {
        assertThat(PushScheduler.deadlineTemplate(hoursLeft)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"PROJECT_CREATED", "INQUIRY_ANSWERED", "SCORE_PUBLISHED"})
    void 그_외_알림은_기존_기본값을_쓴다(PushMessageTemplate template) {
        assertThat(template.getPushOptions()).isEqualTo(PushOptions.DEFAULT);
    }
}
