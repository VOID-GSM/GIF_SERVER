package com.example.gifserverv2.domain.score.service;

import com.example.gifserverv2.domain.score.dto.request.CreateMajorScoreRequest;
import com.example.gifserverv2.domain.score.entity.Score;
import com.example.gifserverv2.domain.project.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MajorScoreService {

    private final ScoreSupport support;

    public void createMajor(CreateMajorScoreRequest request, String evaluatorId) {
        support.validateCommonRequest(request.getProjectId(), evaluatorId);
        support.requireScore(request.getTechnicalCompleteness(), "technicalCompleteness");
        support.requireScore(request.getSocialValueMajor(), "socialValueMajor");
        support.requireScore(request.getAiUtilityMajorScore(), "aiUtilityMajorScore");
        support.requireScore(request.getPresentationMajor(), "presentationMajor");

        Project project = support.getProjectOrThrow(request.getProjectId());
        final String evaluatorKey = evaluatorId.trim();
        support.upsertScore(
                project,
                evaluatorKey,
                () -> Score.builder()
                        .project(project)
                        .evaluatorId(evaluatorKey)
                        .technicalCompleteness(request.getTechnicalCompleteness())
                        .socialValueMajor(request.getSocialValueMajor())
                        .aiUtilizationMajor(request.getAiUtilityMajorScore())
                        .presentationMajor(request.getPresentationMajor())
                        .reportWriting(0)
                        .reportContent(0)
                        .aiUsagePlan(0)
                        .creativity(0)
                        .userExperience(0)
                        .socialValueCommunity(0)
                        .aiUtilizationCommunity(0)
                        .presentationCommunity(0)
                        .build(),
                score -> score.updateScore(
                        request.getTechnicalCompleteness(),
                        request.getSocialValueMajor(),
                        request.getAiUtilityMajorScore(),
                        request.getPresentationMajor(),
                        score.getReportWriting(),
                        score.getReportContent(),
                        score.getAiUsagePlan(),
                        score.getCreativity(),
                        score.getUserExperience(),
                        score.getSocialValueCommunity(),
                        score.getAiUtilizationCommunity(),
                        score.getPresentationCommunity()
                )
        );
    }

    public void updateMajor(CreateMajorScoreRequest request, String evaluatorId) {
        support.validateCommonRequest(request.getProjectId(), evaluatorId);
        support.requireScore(request.getTechnicalCompleteness(), "technicalCompleteness");
        support.requireScore(request.getSocialValueMajor(), "socialValueMajor");
        support.requireScore(request.getAiUtilityMajorScore(), "aiUtilityMajorScore");
        support.requireScore(request.getPresentationMajor(), "presentationMajor");

        Project project = support.getProjectOrThrow(request.getProjectId());
        Score score = support.getScoreOrThrow(project, evaluatorId.trim());

        score.updateScore(
                request.getTechnicalCompleteness(),
                request.getSocialValueMajor(),
                request.getAiUtilityMajorScore(),
                request.getPresentationMajor(),
                score.getReportWriting(),
                score.getReportContent(),
                score.getAiUsagePlan(),
                score.getCreativity(),
                score.getUserExperience(),
                score.getSocialValueCommunity(),
                score.getAiUtilizationCommunity(),
                score.getPresentationCommunity()
        );
    }

    public Score getMajor(Long projectId, String evaluatorId) {
        support.validateCommonRequest(projectId, evaluatorId);
        Project project = support.getProjectOrThrow(projectId);
        return support.getScoreOrThrow(project, evaluatorId.trim());
    }
}

