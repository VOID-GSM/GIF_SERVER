package com.example.gifserverv2.domain.ai.service;

import com.example.gifserverv2.domain.form.entity.FormFieldAnswer;
import com.example.gifserverv2.domain.form.entity.FormSubmit;
import com.example.gifserverv2.domain.form.repository.FormSubmitRepository;
import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.exception.ProjectException;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * AiSummaryService의 DB 조회/저장만 담당하는 트랜잭션 경계.
 * OpenAI 호출(수 초 소요, 블로킹)이 DB 트랜잭션 안에서 일어나
 * 커넥션 풀을 오래 점유하는 걸 막기 위해 AiSummaryService에서 분리했다.
 * (ClientInquiryService / InquiryWriterService와 동일한 분리 패턴)
 */
@Service
@RequiredArgsConstructor
public class AiSummaryPersistenceService {

    private final ProjectRepository projectRepository;
    private final FormSubmitRepository formSubmitRepository;

    public record SummaryContext(String cachedSummary, String prompt) {
        public static SummaryContext cached(String summary) {
            return new SummaryContext(summary, null);
        }

        public static SummaryContext toGenerate(String prompt) {
            return new SummaryContext(null, prompt);
        }
    }

    @Transactional(readOnly = true)
    public SummaryContext loadProjectContext(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectException::notFound);

        if (project.getAiSummary() != null) {
            return SummaryContext.cached(project.getAiSummary());
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("다음 프로젝트 정보를 한줄로 요약해주세요:\n\n");
        prompt.append("프로젝트명: ").append(project.getName()).append("\n");
        prompt.append("팀명: ").append(project.getTeamName()).append("\n");
        if (project.getDescription() != null) {
            prompt.append("설명: ").append(project.getDescription()).append("\n");
        }

        return SummaryContext.toGenerate(prompt.toString());
    }

    @Transactional
    public void saveProjectSummary(Long projectId, String summary) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectException::notFound);
        project.updateAiSummary(summary);
        projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public SummaryContext loadFormSubmitContext(Long submitId) {
        FormSubmit submit = formSubmitRepository.findByIdWithAnswersAndFields(submitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "제출 내역을 찾을 수 없습니다."));

        if (submit.getAiSummary() != null) {
            return SummaryContext.cached(submit.getAiSummary());
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("다음 양식 제출 내용을 한줄로 요약해주세요:\n\n");
        prompt.append("양식명: ").append(submit.getForm().getTitle()).append("\n");
        prompt.append("제출 답변:\n");

        for (FormFieldAnswer answer : submit.getAnswers()) {
            String fieldTitle = answer.getFormField().getTitle();
            switch (answer.getFormField().getType()) {
                case TEXT -> {
                    if (answer.getTextAnswer() != null) {
                        prompt.append("- ").append(fieldTitle).append(": ").append(answer.getTextAnswer()).append("\n");
                    }
                }
                case CALENDAR -> {
                    if (answer.getCalendarEvents() != null && !answer.getCalendarEvents().isEmpty()) {
                        for (var event : answer.getCalendarEvents()) {
                            prompt.append("- ").append(fieldTitle).append(": ")
                                    .append(event.getEventName())
                                    .append(" (").append(event.getStartDate()).append(" ~ ").append(event.getEndDate()).append(")\n");
                        }
                    }
                }
                case FILE -> {
                    if (answer.getFilePath() != null) {
                        prompt.append("- ").append(fieldTitle).append(": 파일 첨부됨\n");
                    }
                }
            }
        }

        return SummaryContext.toGenerate(prompt.toString());
    }

    @Transactional
    public void saveFormSubmitSummary(Long submitId, String summary) {
        FormSubmit submit = formSubmitRepository.findById(submitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "제출 내역을 찾을 수 없습니다."));
        submit.updateAiSummary(summary);
        formSubmitRepository.save(submit);
    }
}
