package com.example.gifserverv2.domain.ai.service;

import com.example.gifserverv2.domain.ai.service.AiSummaryPersistenceService.JapaneseSummaryContext;
import com.example.gifserverv2.domain.ai.service.AiSummaryPersistenceService.SummaryContext;
import com.example.gifserverv2.global.ai.OpenAiService;
import com.example.gifserverv2.global.i18n.Language;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSummaryService {

    private static final int MAX_SUMMARY_JA_LENGTH = 1_000;

    private final AiSummaryPersistenceService persistenceService;
    private final OpenAiService openAiService;
    private final AiTranslationService translationService;

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

        if (!openAiService.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI 기능이 아직 설정되지 않았습니다.");
        }

        String summary;
        try {
            summary = openAiService.summarize(context.prompt());
        } catch (RuntimeException e) {
            log.warn("프로젝트 {} AI 요약 호출 실패", projectId, e);
            throw aiFailed();
        }
        if (summary == null || summary.isBlank()) {
            throw aiFailed();
        }

        summary = summary.strip();
        persistenceService.saveProjectSummary(projectId, summary);
        return summary;
    }

    private static ResponseStatusException aiFailed() {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI 응답을 받지 못했습니다. 잠시 후 다시 시도해주세요.");
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
