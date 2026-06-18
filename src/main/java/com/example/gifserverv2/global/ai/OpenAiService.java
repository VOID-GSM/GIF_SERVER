package com.example.gifserverv2.global.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    @Value("${OPEN_API_KEY}")
    private String apiKey;

    private final ObjectMapper objectMapper;
    private final OkHttpClient client = new OkHttpClient();

    public String summarize(String prompt) {
        try {
            OkHttpClient client = new OkHttpClient();

            Map<String, Object> body = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", List.of(
                            Map.of("role", "system", "content", "당신은 학교 프로젝트 관리 시스템의 AI 요약 도우미입니다. 주어진 정보를 한국어로 2-3문장으로 간결하게 요약해주세요."),
                            Map.of("role", "user", "content", prompt)
                    ),
                    "max_tokens", 300
            );

            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json"),
                    objectMapper.writeValueAsString(body)
            );
            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .post(requestBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();
                Map<?, ?> json = objectMapper.readValue(responseBody, Map.class);
                List<?> choices = (List<?>) json.get("choices");
                Map<?, ?> message = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
                return (String) message.get("content");
            }
        } catch (Exception e) {
            throw new RuntimeException("AI 요약 중 오류가 발생했습니다.", e);
        }
    }
}
