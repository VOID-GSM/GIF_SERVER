package com.example.gifserverv2.global.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class LenientLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        if (text == null || text.isBlank()) {
            return null;
        }

        text = text.trim();

        try {
            return LocalDateTime.parse(text);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(text).atTime(LocalTime.MAX.withNano(0));
            } catch (DateTimeParseException e2) {
                throw ctxt.weirdStringException(text, LocalDateTime.class,
                        "날짜 형식이 올바르지 않습니다. 'yyyy-MM-ddTHH:mm:ss' 또는 'yyyy-MM-dd' 형식이어야 합니다.");
            }
        }
    }
}
