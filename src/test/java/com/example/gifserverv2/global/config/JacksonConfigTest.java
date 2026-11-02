package com.example.gifserverv2.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonConfigTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
            .withUserConfiguration(JacksonConfig.class);

    @Test
    void LocalDateTime은_ISO_문자열로_직렬화된다() {
        runner.run(context -> {
            ObjectMapper mapper = context.getBean(ObjectMapper.class);
            String json = mapper.writeValueAsString(Map.of("createdAt", LocalDateTime.of(2026, 10, 31, 16, 25, 45)));
            assertThat(json).isEqualTo("{\"createdAt\":\"2026-10-31T16:25:45\"}");
        });
    }

    @Test
    void 기존_관대한_LocalDateTime_역직렬화는_유지된다() {
        runner.run(context -> {
            ObjectMapper mapper = context.getBean(ObjectMapper.class);
            Holder holder = mapper.readValue("{\"value\":\"2026-10-31T16:25:45\"}", Holder.class);
            assertThat(holder.value).isEqualTo(LocalDateTime.of(2026, 10, 31, 16, 25, 45));

            Holder dateOnly = mapper.readValue("{\"value\":\"2026-10-31\"}", Holder.class);
            assertThat(dateOnly.value).isEqualTo(LocalDateTime.of(2026, 10, 31, 23, 59, 59));
        });
    }

    static class Holder {
        public LocalDateTime value;
    }
}
