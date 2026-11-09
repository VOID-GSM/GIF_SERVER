package com.example.gifserverv2.domain.ai.service;

import com.example.gifserverv2.domain.ai.dto.request.RetrospectiveAiRequest;
import com.example.gifserverv2.domain.ai.dto.response.RetrospectiveStructureResponse;
import com.example.gifserverv2.domain.retrospective.entity.RetrospectiveCategory;
import com.example.gifserverv2.global.ai.OpenAiService;
import com.example.gifserverv2.global.ai.OpenAiService.ChatResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RetrospectiveAiServiceTest {

    private static final String CONTENT = "로그인 토큰이 만료되면 새로고침해도 다시 로그인 화면으로 돌아가는 문제가 있었다.";

    @Mock OpenAiService openAiService;
    @Mock AiUsageLimiter usageLimiter;
    RetrospectiveAiService service;

    @BeforeEach
    void setUp() {
        service = new RetrospectiveAiService(openAiService, usageLimiter, new ObjectMapper());
    }

    private static RetrospectiveAiRequest request(String content) {
        return new RetrospectiveAiRequest("로그인 토큰 갱신 문제 해결", RetrospectiveCategory.TROUBLESHOOTING, content);
    }

    private static HttpStatus statusOf(Throwable e) {
        return HttpStatus.valueOf(((ResponseStatusException) e).getStatusCode().value());
    }

    @Test
    void 다듬기는_카테고리_제목_본문을_담아_요청하고_결과를_돌려준다() {
        given(openAiService.isConfigured()).willReturn(true);
        given(openAiService.generate(anyString(), anyString(), anyInt(), eq(false)))
                .willReturn(new ChatResult("다듬은 본문", false));

        assertThat(service.polish(1L, request(CONTENT)).content()).isEqualTo("다듬은 본문");

        ArgumentCaptor<String> userPrompt = ArgumentCaptor.forClass(String.class);
        verify(openAiService).generate(anyString(), userPrompt.capture(), anyInt(), eq(false));
        assertThat(userPrompt.getValue())
                .contains("트러블슈팅", "로그인 토큰 갱신 문제 해결", "<retrospective>\n" + CONTENT + "\n</retrospective>");
        verify(usageLimiter).acquire(1L);
    }

    @Test
    void 결과_전체를_감싼_코드_블록은_벗겨낸다() {
        given(openAiService.isConfigured()).willReturn(true);
        given(openAiService.generate(anyString(), anyString(), anyInt(), anyBoolean()))
                .willReturn(new ChatResult("```markdown\n## 문제\n내용\n```", false));

        assertThat(service.toPortfolio(1L, request(CONTENT)).content()).isEqualTo("## 문제\n내용");
    }

    @Test
    void 본문이_짧으면_400이고_OpenAI를_호출하지_않는다() {
        assertThatThrownBy(() -> service.polish(1L, request("  짧은 글  ")))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.BAD_REQUEST))
                .hasMessageContaining("20자 이상");
        assertThatThrownBy(() -> service.toPortfolio(1L, request(null)))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(usageLimiter, never()).acquire(anyLong());
        verify(openAiService, never()).generate(anyString(), anyString(), anyInt(), anyBoolean());
    }

    @Test
    void 구조_추천은_본문이_없어도_목차와_템플릿을_돌려준다() {
        given(openAiService.isConfigured()).willReturn(true);
        given(openAiService.generate(anyString(), anyString(), anyInt(), eq(true)))
                .willReturn(new ChatResult(
                        "{\"sections\": [\"문제 상황\", \" 원인 분석 \", \"\"], \"content\": \"## 문제 상황\\n> 어떤 문제였나요?\"}",
                        false));

        RetrospectiveStructureResponse response = service.recommendStructure(1L, request(""));

        assertThat(response.sections()).containsExactly("문제 상황", "원인 분석");
        assertThat(response.content()).isEqualTo("## 문제 상황\n> 어떤 문제였나요?");
    }

    @Test
    void 구조_추천_응답이_JSON이_아니면_502() {
        given(openAiService.isConfigured()).willReturn(true);
        given(openAiService.generate(anyString(), anyString(), anyInt(), eq(true)))
                .willReturn(new ChatResult("목차는 다음과 같습니다", false));

        assertThatThrownBy(() -> service.recommendStructure(1L, request(CONTENT)))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.BAD_GATEWAY));
    }

    @Test
    void OpenAI_호출이_실패하면_502이고_사용_횟수를_돌려준다() {
        given(openAiService.isConfigured()).willReturn(true);
        given(openAiService.generate(anyString(), anyString(), anyInt(), anyBoolean()))
                .willThrow(new ResourceAccessException("timeout"));

        assertThatThrownBy(() -> service.polish(1L, request(CONTENT)))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.BAD_GATEWAY));
        verify(usageLimiter).release(1L);
    }

    @Test
    void 결과가_길이_제한에_잘리면_400이고_사용_횟수를_돌려준다() {
        given(openAiService.isConfigured()).willReturn(true);
        given(openAiService.generate(anyString(), anyString(), anyInt(), anyBoolean()))
                .willReturn(new ChatResult("중간에 끊긴 글", true));

        assertThatThrownBy(() -> service.polish(1L, request(CONTENT)))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(usageLimiter).release(1L);
    }

    @Test
    void API_키가_없으면_503() {
        given(openAiService.isConfigured()).willReturn(false);

        assertThatThrownBy(() -> service.polish(1L, request(CONTENT)))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
        verify(usageLimiter, never()).acquire(anyLong());
    }
}
