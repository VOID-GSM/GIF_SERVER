package com.example.gifserverv2.domain.ai.service;

import com.example.gifserverv2.domain.form.entity.FormFieldAnswer;
import com.example.gifserverv2.domain.form.entity.FormSubmit;
import com.example.gifserverv2.domain.form.repository.FormSubmitRepository;
import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.exception.ProjectException;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.global.ai.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiSummaryService {

    private final ProjectRepository projectRepository;
    private final FormSubmitRepository formSubmitRepository;
    private final OpenAiService openAiService;

    public String summarizeProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectException::notFound);

        StringBuilder prompt = new StringBuilder();
        prompt.append("다음 프로젝트 정보를 요약해주세요:\n\n");
        prompt.append("프로젝트명: ").append(project.getName()).append("\n");
        prompt.append("팀명: ").append(project.getTeamName()).append("\n");
        if (project.getDescription() != null) {
            prompt.append("설명: ").append(project.getDescription()).append("\n");
        }

        return openAiService.summarize(prompt.toString());
    }

    public String summarizeFormSubmit(Long submitId) {
        FormSubmit submit = formSubmitRepository.findById(submitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "제출 내역을 찾을 수 없습니다."));

        StringBuilder prompt = new StringBuilder();
        prompt.append("다음 양식 제출 내용을 요약해주세요:\n\n");
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
                    if (answer.getEventName() != null) {
                        prompt.append("- ").append(fieldTitle).append(": ")
                                .append(answer.getEventName())
                                .append(" (").append(answer.getStartDate()).append(" ~ ").append(answer.getEndDate()).append(")\n");
                    }
                }
                case FILE -> {
                    if (answer.getFilePath() != null) {
                        prompt.append("- ").append(fieldTitle).append(": 파일 첨부됨\n");
                    }
                }
            }
        }

        return openAiService.summarize(prompt.toString());
    }
}