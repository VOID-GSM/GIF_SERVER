package com.example.gifserverv2.global.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    private static final String CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o-mini";
    private static final String SUMMARY_SYSTEM_PROMPT =
            "당신은 학교 프로젝트 관리 시스템의 AI 요약 도우미입니다. 주어진 정보를 한국어로 한문장으로 간결하게 요약해주세요.";

    private static final int CONNECT_TIMEOUT_MS = 3_000;
    private static final int READ_TIMEOUT_MS = 10_000;
    private static final int GENERATE_READ_TIMEOUT_MS = 90_000;

    @Value("${openai.api-key}")
    private String apiKey;

    private final RestClient restClient = createRestClient(READ_TIMEOUT_MS);
    private final RestClient generateRestClient = createRestClient(GENERATE_READ_TIMEOUT_MS);

    public record ChatResult(String content, boolean truncated) {}

    private static RestClient createRestClient(int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        factory.setReadTimeout(readTimeoutMs);
        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String summarize(String prompt) {
        return request(restClient, SUMMARY_SYSTEM_PROMPT, prompt, 300, false).content();
    }

    // jsonResponse 를 쓰려면 프롬프트에 "JSON" 이 들어 있어야 한다. (OpenAI response_format 제약)
    public ChatResult generate(String systemPrompt, String userPrompt, int maxTokens, boolean jsonResponse) {
        return request(generateRestClient, systemPrompt, userPrompt, maxTokens, jsonResponse);
    }

    @SuppressWarnings("unchecked")
    private ChatResult request(RestClient client, String systemPrompt, String userPrompt, int maxTokens, boolean jsonResponse) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", MODEL);
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));
        body.put("max_tokens", maxTokens);
        if (jsonResponse) {
            body.put("response_format", Map.of("type", "json_object"));
        }

        Map<String, Object> response = client.post()
                .uri(CHAT_COMPLETIONS_URL)
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

        boolean truncated = "length".equals(choices.get(0).get("finish_reason"));
        return new ChatResult((String) message.get("content"), truncated);
    }
}
