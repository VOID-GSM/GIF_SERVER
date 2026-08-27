package com.example.gifserverv2.domain.ai.service;

import com.example.gifserverv2.domain.ai.service.AiSummaryPersistenceService.SummaryContext;
import com.example.gifserverv2.global.ai.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiSummaryService {

    private final AiSummaryPersistenceService persistenceService;
    private final OpenAiService openAiService;

    /**
     * DB 조회/저장은 AiSummaryPersistenceService의 짧은 트랜잭션으로 처리하고,
     * OpenAI 호출(블로킹 I/O)은 트랜잭션 밖에서 수행해 DB 커넥션을 점유하지 않는다.
     */
    public String summarizeProject(Long projectId) {
        SummaryContext context = persistenceService.loadProjectContext(projectId);
        if (context.cachedSummary() != null) {
            return context.cachedSummary();
        }

        String summary = openAiService.summarize(context.prompt());
        persistenceService.saveProjectSummary(projectId, summary);
        return summary;
    }

    public String summarizeFormSubmit(Long submitId) {
        SummaryContext context = persistenceService.loadFormSubmitContext(submitId);
        if (context.cachedSummary() != null) {
            return context.cachedSummary();
        }

        String summary = openAiService.summarize(context.prompt());
        persistenceService.saveFormSubmitSummary(submitId, summary);
        return summary;
    }
}
