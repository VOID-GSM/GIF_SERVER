package com.example.gifserverv2.domain.assessment.roadmaking.service;

import com.example.gifserverv2.domain.assessment.roadmaking.model.Board;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Position;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

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

    public String toFencesJson(List<Position> fences) {
        try {
            return objectMapper.writeValueAsString(fences);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("울타리 위치 직렬화에 실패했습니다.", e);
        }
    }

    public List<Position> fencesFromJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("울타리 위치 역직렬화에 실패했습니다.", e);
        }
    }
}