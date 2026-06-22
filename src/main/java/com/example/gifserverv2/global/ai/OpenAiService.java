package com.example.gifserverv2.global.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    @Value("${OPEN_API_KEY}")
    private String apiKey;

    private final RestClient restClient = RestClient.create();

    @SuppressWarnings("unchecked")
    public String summarize(String prompt) {
        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "system", "content", "당신은 학교 프로젝트 관리 시스템의 AI 요약 도우미입니다. 주어진 정보를 한국어로 2-3문장으로 간결하게 요약해주세요."),
                        Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 300
        );

        Map<String, Object> response = restClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new RuntimeException("OpenAI API 응답이 비어 있습니다.");
        }

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("OpenAI API 응답에서 결과를 찾을 수 없습니다.");
        }

        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        if (message == null) {
            throw new RuntimeException("OpenAI API 응답 메시지가 비어 있습니다.");
        }

        return (String) message.get("content");
    }
}