package com.example.gifserverv2.global.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class JacksonConfig {
    /**
     * ObjectMapper를 직접 생성하면 Spring Boot 기본 설정(spring.jackson.*, ISO 날짜 직렬화,
     * 알 수 없는 필드 무시)이 모두 꺼지므로, Boot가 만드는 ObjectMapper에 커스터마이징만 얹는다.
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer lenientLocalDateTimeCustomizer() {
        return builder -> builder.deserializerByType(LocalDateTime.class, new LenientLocalDateTimeDeserializer());
    }
}
