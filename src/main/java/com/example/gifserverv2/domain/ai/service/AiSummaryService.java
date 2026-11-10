package com.example.gifserverv2.domain.ai.service;

import com.example.gifserverv2.domain.ai.service.AiSummaryPersistenceService.JapaneseSummaryContext;
import com.example.gifserverv2.domain.ai.service.AiSummaryPersistenceService.SummaryContext;
import com.example.gifserverv2.global.ai.OpenAiService;
import com.example.gifserverv2.global.i18n.Language;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSummaryService {

    private static final int MAX_SUMMARY_JA_LENGTH = 1_000;

    private final AiSummaryPersistenceService persistenceService;
    private final OpenAiService openAiService;
    private final AiTranslationService translationService;

    // GET /api/project/{projectId}/summary 는 비용 문제로 아직 주석 처리되어 있다. (#202)
    public String summarizeProject(Long projectId, Language lang) {
        String summary = summarizeProjectInKorean(projectId);
        if (lang != Language.JA) {
            return summary;
        }
        return translateProjectSummary(projectId, summary);
    }

    private String summarizeProjectInKorean(Long projectId) {
        SummaryContext context = persistenceService.loadProjectContext(projectId);
        if (context.cachedSummary() != null) {
            return context.cachedSummary();
        }

        String summary = openAiService.summarize(context.prompt());
        persistenceService.saveProjectSummary(projectId, summary);
        return summary;
    }

    // 일본어 요약은 한국어 요약을 번역해 따로 저장한다. 번역에 실패하면 한국어 요약을 준다.
    private String translateProjectSummary(Long projectId, String summary) {
        JapaneseSummaryContext context = persistenceService.loadProjectJapaneseSummaryContext(projectId);
        if (context.cachedSummaryJa() != null) {
            return context.cachedSummaryJa();
        }

        String summaryJa;
        try {
            summaryJa = translationService.translateToJapanese(summary, List.of(context.projectName(), context.teamName()));
        } catch (Exception e) {
            log.warn("[Translation] 프로젝트 {} AI 요약 일본어 번역에 실패했습니다: {}", projectId, e.getMessage());
            return summary;
        }
        if (summaryJa.length() <= MAX_SUMMARY_JA_LENGTH) {
            persistenceService.saveProjectSummaryJa(projectId, summary, summaryJa);
        }
        return summaryJa;
    }

//    public String summarizeFormSubmit(Long submitId) {
//        SummaryContext context = persistenceService.loadFormSubmitContext(submitId);
//        if (context.cachedSummary() != null) {
//            return context.cachedSummary();
//        }
//
//        String summary = openAiService.summarize(context.prompt());
//        persistenceService.saveFormSubmitSummary(submitId, summary);
//        return summary;
//    }
}
