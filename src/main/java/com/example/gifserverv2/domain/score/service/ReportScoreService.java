package com.example.gifserverv2.domain.score.service;

import com.example.gifserverv2.domain.score.dto.request.CreateReportScoreRequest;
import com.example.gifserverv2.domain.score.entity.Score;
import com.example.gifserverv2.domain.project.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportScoreService {

    private final ScoreSupport support;

    public void createReport(CreateReportScoreRequest request) {
        support.validateCommonRequest(request.getProjectId(), request.getEvaluatorId());
        support.requireScore(request.getReportWriting(), "reportWriting");
        support.requireScore(request.getReportContent(), "reportContent");
        support.requireScore(request.getAiUsagePlan(), "aiUsagePlan");
        support.requireScore(request.getCreativity(), "creativity");

        Project project = support.getProjectOrThrow(request.getProjectId());
        if (support.scoreRepository().existsByProjectAndEvaluatorId(project, request.getEvaluatorId().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 해당 평가자의 점수가 존재합니다.");
        }

        Score score = Score.builder()
                .project(project)
                .evaluatorId(request.getEvaluatorId().trim())
                .technicalCompleteness(0)
                .socialValueMajor(0)
                .aiUtilizationMajor(0)
                .presentationMajor(0)
                .reportWriting(request.getReportWriting())
                .reportContent(request.getReportContent())
                .aiUsagePlan(request.getAiUsagePlan())
                .creativity(request.getCreativity())
                .userExperience(0)
                .socialValueCommunity(0)
                .aiUtilizationCommunity(0)
                .presentationCommunity(0)
                .build();

        support.scoreRepository().save(score);
    }

    public void updateReport(CreateReportScoreRequest request) {
        support.validateCommonRequest(request.getProjectId(), request.getEvaluatorId());
        support.requireScore(request.getReportWriting(), "reportWriting");
        support.requireScore(request.getReportContent(), "reportContent");
        support.requireScore(request.getAiUsagePlan(), "aiUsagePlan");
        support.requireScore(request.getCreativity(), "creativity");

        Project project = support.getProjectOrThrow(request.getProjectId());
        Score score = support.getScoreOrThrow(project, request.getEvaluatorId().trim());

        score.updateScore(
                score.getTechnicalCompleteness(),
                score.getSocialValueMajor(),
                score.getAiUtilizationMajor(),
                score.getPresentationMajor(),
                request.getReportWriting(),
                request.getReportContent(),
                request.getAiUsagePlan(),
                request.getCreativity(),
                score.getUserExperience(),
                score.getSocialValueCommunity(),
                score.getAiUtilizationCommunity(),
                score.getPresentationCommunity()
        );
    }

    public Score getReport(Long projectId, String evaluatorId) {
        support.validateCommonRequest(projectId, evaluatorId);
        Project project = support.getProjectOrThrow(projectId);
        return support.getScoreOrThrow(project, evaluatorId.trim());
    }
}

