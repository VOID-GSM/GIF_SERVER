package com.example.gifserverv2.domain.score.service;

import com.example.gifserverv2.domain.score.dto.request.CreateReportScoreRequest;
import com.example.gifserverv2.domain.score.entity.Score;
import com.example.gifserverv2.domain.project.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        String evaluatorId = request.getEvaluatorId().trim();
        support.scoreRepository().findByProjectAndEvaluatorId(project, evaluatorId)
                .ifPresentOrElse(
                        score -> score.updateScore(
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
                        ),
                        () -> support.scoreRepository().save(Score.builder()
                                .project(project)
                                .evaluatorId(evaluatorId)
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
                                .build())
                );
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

