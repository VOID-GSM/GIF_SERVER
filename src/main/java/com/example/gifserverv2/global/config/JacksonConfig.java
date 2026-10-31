package com.example.gifserverv2.global.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class JacksonConfig {

    // ObjectMapper를 직접 만들면 application.yml의 spring.jackson 설정이 무시되므로 Boot 기본 빌더에 덧붙인다.
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer lenientLocalDateTimeCustomizer() {
        return builder -> builder.deserializerByType(LocalDateTime.class, new LenientLocalDateTimeDeserializer());
    }
}
