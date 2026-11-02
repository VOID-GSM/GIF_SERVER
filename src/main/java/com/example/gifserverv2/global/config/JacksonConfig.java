package com.example.gifserverv2.global.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class JacksonConfig {

    // ObjectMapper를 직접 만들면 application.yml의 spring.jackson 설정이 무시되므로 Boot 기본 빌더에 덧붙인다.
    // 참고: Boot 기본 빌더는 FAIL_ON_UNKNOWN_PROPERTIES=false 라서, 요청에 모르는 필드가 오면 에러 없이 무시된다.
    //       (필드명을 잘못 보내면 해당 값은 null 로 들어오므로 필수 값은 @NotNull 등으로 검증할 것)
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer lenientLocalDateTimeCustomizer() {
        return builder -> builder.deserializerByType(LocalDateTime.class, new LenientLocalDateTimeDeserializer());
    }
}
