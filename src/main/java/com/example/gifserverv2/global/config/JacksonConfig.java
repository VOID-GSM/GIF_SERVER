package com.example.gifserverv2.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.time.LocalDateTime;

@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        SimpleModule lenientDateTimeModule = new SimpleModule();
        lenientDateTimeModule.addDeserializer(LocalDateTime.class, new LenientLocalDateTimeDeserializer());
        objectMapper.registerModule(lenientDateTimeModule);

        return objectMapper;
    }
}
