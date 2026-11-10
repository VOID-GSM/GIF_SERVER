package com.example.gifserverv2.domain.ai.service;

import com.example.gifserverv2.domain.ai.service.AiSummaryPersistenceService.JapaneseSummaryContext;
import com.example.gifserverv2.domain.ai.service.AiSummaryPersistenceService.SummaryContext;
import com.example.gifserverv2.global.ai.OpenAiService;
import com.example.gifserverv2.global.i18n.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AiSummaryServiceTest {

    private static final Long PROJECT_ID = 1L;
    private static final String SUMMARY = "GIF는 아이디어 페스티벌 운영 플랫폼이다.";
    private static final String SUMMARY_JA = "GIFはアイデアフェスティバル運営プラットフォームです。";

    @Mock AiSummaryPersistenceService persistenceService;
    @Mock OpenAiService openAiService;
    @Mock AiTranslationService translationService;
    AiSummaryService service;

    @BeforeEach
    void setUp() {
        service = new AiSummaryService(persistenceService, openAiService, translationService);
        given(persistenceService.loadProjectContext(PROJECT_ID)).willReturn(SummaryContext.cached(SUMMARY));
    }

    @Test
    void 한국어_요약은_번역하지_않는다() {
        assertThat(service.summarizeProject(PROJECT_ID, Language.KO)).isEqualTo(SUMMARY);

        verify(translationService, never()).translateToJapanese(any(), any());
    }

    @Test
    void 저장된_일본어_요약이_있으면_다시_번역하지_않는다() {
        given(persistenceService.loadProjectJapaneseSummaryContext(PROJECT_ID))
                .willReturn(new JapaneseSummaryContext(SUMMARY_JA, "GIF", "VOID"));

        assertThat(service.summarizeProject(PROJECT_ID, Language.JA)).isEqualTo(SUMMARY_JA);

        verify(translationService, never()).translateToJapanese(any(), any());
        verify(openAiService, never()).summarize(anyString());
    }

    @Test
    void 일본어_요약이_없으면_한국어_요약을_번역해_저장한다() {
        given(persistenceService.loadProjectJapaneseSummaryContext(PROJECT_ID))
                .willReturn(new JapaneseSummaryContext(null, "GIF", "VOID"));
        given(translationService.translateToJapanese(SUMMARY, List.of("GIF", "VOID"))).willReturn(SUMMARY_JA);

        assertThat(service.summarizeProject(PROJECT_ID, Language.JA)).isEqualTo(SUMMARY_JA);

        verify(persistenceService).saveProjectSummaryJa(PROJECT_ID, SUMMARY, SUMMARY_JA);
    }

    @Test
    void 번역에_실패하면_한국어_요약을_준다() {
        given(persistenceService.loadProjectJapaneseSummaryContext(PROJECT_ID))
                .willReturn(new JapaneseSummaryContext(null, "GIF", "VOID"));
        given(translationService.translateToJapanese(any(), any())).willThrow(new ResourceAccessException("timeout"));

        assertThat(service.summarizeProject(PROJECT_ID, Language.JA)).isEqualTo(SUMMARY);

        verify(persistenceService, never()).saveProjectSummaryJa(any(), any(), any());
    }
}
