package com.example.gifserverv2.domain.assessment.roadmaking.service;

import com.example.gifserverv2.domain.assessment.roadmaking.model.Board;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoardMapper {
    private final ObjectMapper objectMapper;

    public String toJson(Board board) {
        try {
            return objectMapper.writeValueAsString(board);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Board 직렬화에 실패했습니다.", e);
        }
    }

    public Board fromJson(String json) {
        try {
            return objectMapper.readValue(json, Board.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Board 역직렬화에 실패했습니다.", e);
        }
    }
}