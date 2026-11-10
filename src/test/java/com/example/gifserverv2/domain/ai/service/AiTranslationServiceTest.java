package com.example.gifserverv2.domain.ai.service;

import com.example.gifserverv2.global.ai.OpenAiService;
import com.example.gifserverv2.global.ai.OpenAiService.ChatResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AiTranslationServiceTest {

    @Mock OpenAiService openAiService;
    AiTranslationService service;

    @BeforeEach
    void setUp() {
        service = new AiTranslationService(openAiService);
    }

    @Test
    void 그대로_둘_고유명사와_원문을_담아_요청하고_번역문을_돌려준다() {
        given(openAiService.isConfigured()).willReturn(true);
        given(openAiService.generate(anyString(), anyString(), anyInt(), eq(false)))
                .willReturn(new ChatResult("  GIFは運営プラットフォームです。\n", false));

        assertThat(service.translateToJapanese("GIF는 운영 플랫폼입니다.", List.of("GIF", "VOID")))
                .isEqualTo("GIFは運営プラットフォームです。");

        ArgumentCaptor<String> systemPrompt = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> userPrompt = ArgumentCaptor.forClass(String.class);
        verify(openAiService).generate(systemPrompt.capture(), userPrompt.capture(), anyInt(), eq(false));
        assertThat(systemPrompt.getValue()).contains("です・ます", "줄바꿈", "마크다운");
        assertThat(userPrompt.getValue())
                .contains("<keep>\nGIF\nVOID\n</keep>")
                .contains("<text>\nGIF는 운영 플랫폼입니다.\n</text>");
    }

    @Test
    void 잘린_번역은_실패로_본다() {
        given(openAiService.isConfigured()).willReturn(true);
        given(openAiService.generate(anyString(), anyString(), anyInt(), anyBoolean()))
                .willReturn(new ChatResult("途中まで", true));

        assertThatThrownBy(() -> service.translateToJapanese("소개", List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 키가_없으면_OpenAI를_호출하지_않는다() {
        given(openAiService.isConfigured()).willReturn(false);

        assertThatThrownBy(() -> service.translateToJapanese("소개", List.of()))
                .isInstanceOf(IllegalStateException.class);
        verify(openAiService, never()).generate(anyString(), anyString(), anyInt(), anyBoolean());
    }
}
