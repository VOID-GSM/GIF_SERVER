package com.example.gifserverv2.domain.score.service;

import com.example.gifserverv2.domain.score.dto.request.CreateSocialScoreRequest;
import com.example.gifserverv2.domain.score.entity.Score;
import com.example.gifserverv2.domain.project.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SocialScoreService {

    private final ScoreSupport support;

    public void createSocial(CreateSocialScoreRequest request, String evaluatorId) {
        support.validateCommonRequest(request.getProjectId(), evaluatorId);
        support.requireScore(request.getUserExperience(), "userExperience");
        support.requireScore(request.getSocialValueCommunity(), "socialValueCommunity");
        support.requireScore(request.getAiUtilizationCommunity(), "aiUtilizationCommunity");
        support.requireScore(request.getPresentationCommunity(), "presentationCommunity");

        Project project = support.getProjectOrThrow(request.getProjectId());
        final String evaluatorKey = evaluatorId.trim();
        support.scoreRepository().findByProjectAndEvaluatorId(project, evaluatorKey)
                .ifPresentOrElse(
                        score -> score.updateScore(
                                score.getTechnicalCompleteness(),
                                score.getSocialValueMajor(),
                                score.getAiUtilizationMajor(),
                                score.getPresentationMajor(),
                                score.getReportWriting(),
                                score.getReportContent(),
                                score.getAiUsagePlan(),
                                score.getCreativity(),
                                request.getUserExperience(),
                                request.getSocialValueCommunity(),
                                request.getAiUtilizationCommunity(),
                                request.getPresentationCommunity()
                        ),
                        () -> support.scoreRepository().save(Score.builder()
                                .project(project)
                                .evaluatorId(evaluatorKey)
                                .technicalCompleteness(0)
                                .socialValueMajor(0)
                                .aiUtilizationMajor(0)
                                .presentationMajor(0)
                                .reportWriting(0)
                                .reportContent(0)
                                .aiUsagePlan(0)
                                .creativity(0)
                                .userExperience(request.getUserExperience())
                                .socialValueCommunity(request.getSocialValueCommunity())
                                .aiUtilizationCommunity(request.getAiUtilizationCommunity())
                                .presentationCommunity(request.getPresentationCommunity())
                                .build())
                );
    }

    public void updateSocial(CreateSocialScoreRequest request, String evaluatorId) {
        support.validateCommonRequest(request.getProjectId(), evaluatorId);
        support.requireScore(request.getUserExperience(), "userExperience");
        support.requireScore(request.getSocialValueCommunity(), "socialValueCommunity");
        support.requireScore(request.getAiUtilizationCommunity(), "aiUtilizationCommunity");
        support.requireScore(request.getPresentationCommunity(), "presentationCommunity");

        Project project = support.getProjectOrThrow(request.getProjectId());
        Score score = support.getScoreOrThrow(project, evaluatorId.trim());

        score.updateScore(
                score.getTechnicalCompleteness(),
                score.getSocialValueMajor(),
                score.getAiUtilizationMajor(),
                score.getPresentationMajor(),
                score.getReportWriting(),
                score.getReportContent(),
                score.getAiUsagePlan(),
                score.getCreativity(),
                request.getUserExperience(),
                request.getSocialValueCommunity(),
                request.getAiUtilizationCommunity(),
                request.getPresentationCommunity()
        );
    }

    public Score getSocial(Long projectId, String evaluatorId) {
        support.validateCommonRequest(projectId, evaluatorId);
        Project project = support.getProjectOrThrow(projectId);
        return support.getScoreOrThrow(project, evaluatorId.trim());
    }
}

