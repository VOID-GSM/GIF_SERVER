package com.example.gifserverv2.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonConfigTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
            .withUserConfiguration(JacksonConfig.class);

    record Sample(LocalDateTime createdAt) {}

    @Test
    void LocalDateTime은_ISO_문자열로_직렬화된다() {
        runner.run(context -> {
            String json = context.getBean(ObjectMapper.class)
                    .writeValueAsString(new Sample(LocalDateTime.of(2026, 10, 31, 22, 0, 0)));

            assertThat(json).isEqualTo("{\"createdAt\":\"2026-10-31T22:00:00\"}");
        });
    }

    @Test
    void 날짜만_있는_값도_역직렬화된다() {
        runner.run(context -> {
            Sample sample = context.getBean(ObjectMapper.class)
                    .readValue("{\"createdAt\":\"2026-10-31\"}", Sample.class);

            assertThat(sample.createdAt()).isEqualTo(LocalDateTime.of(2026, 10, 31, 23, 59, 59));
        });
    }

    @Test
    void 알_수_없는_필드는_무시된다() {
        runner.run(context -> {
            Sample sample = context.getBean(ObjectMapper.class)
                    .readValue("{\"createdAt\":\"2026-10-31T22:00:00\",\"unknown\":1}", Sample.class);

            assertThat(sample.createdAt()).isEqualTo(LocalDateTime.of(2026, 10, 31, 22, 0, 0));
        });
    }
}
