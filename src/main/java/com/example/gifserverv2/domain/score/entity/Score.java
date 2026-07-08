package com.example.gifserverv2.domain.score.entity;

import com.example.gifserverv2.domain.project.entity.Project;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "evaluator_id", nullable = false)
    private String evaluatorId;

    @Column(name = "technical_score", nullable = false)
    private Integer technicalCompleteness;

    @Column(name = "social_value_score", nullable = false)
    private Integer socialValueMajor;

    @Column(name = "ai_utility_score", nullable = false)
    private Integer aiUtilizationMajor;

    @Column(name = "presentation_score", nullable = false)
    private Integer presentationMajor;

    @Column(name = "report_writing", nullable = false)
    private Integer reportWriting;

    @Column(name = "report_content", nullable = false)
    private Integer reportContent;

    @Column(name = "ai_usage_plan", nullable = false)
    private Integer aiUsagePlan;

    @Column(name = "creativity", nullable = false)
    private Integer creativity;

    @Column(name = "user_experience", nullable = false)
    private Integer userExperience;

    @Column(name = "social_value_community", nullable = false)
    private Integer socialValueCommunity;

    @Column(name = "ai_utilization_community", nullable = false)
    private Integer aiUtilizationCommunity;

    @Column(name = "presentation_community", nullable = false)
    private Integer presentationCommunity;

    @Column(name = "sub_total_score", nullable = false)
    private Integer subTotalScore;

    @Column(name = "score_rank")
    private int rank;

    @Builder
    public Score(Project project, String evaluatorId, Integer technicalCompleteness, Integer socialValueMajor, Integer aiUtilizationMajor, Integer presentationMajor, Integer reportWriting, Integer reportContent, Integer aiUsagePlan, Integer creativity, Integer userExperience, Integer socialValueCommunity, Integer aiUtilizationCommunity, Integer presentationCommunity) {
        this.project = project;
        this.evaluatorId = evaluatorId;
        this.technicalCompleteness = technicalCompleteness;
        this.socialValueMajor = socialValueMajor;
        this.aiUtilizationMajor = aiUtilizationMajor;
        this.presentationMajor = presentationMajor;
        this.reportWriting = reportWriting;
        this.reportContent = reportContent;
        this.aiUsagePlan = aiUsagePlan;
        this.creativity = creativity;
        this.userExperience = userExperience;
        this.socialValueCommunity = socialValueCommunity;
        this.aiUtilizationCommunity = aiUtilizationCommunity;
        this.presentationCommunity = presentationCommunity;
        calculateSubTotalScore();
    }

    public void updateScore(Integer technicalCompleteness, Integer socialValueMajor, Integer aiUtilizationMajor, Integer presentationMajor, Integer reportWriting, Integer reportContent, Integer aiUsagePlan, Integer creativity, Integer userExperience, Integer socialValueCommunity, Integer aiUtilizationCommunity, Integer presentationCommunity) {
        this.technicalCompleteness = technicalCompleteness;
        this.socialValueMajor = socialValueMajor;
        this.aiUtilizationMajor = aiUtilizationMajor;
        this.presentationMajor = presentationMajor;
        this.reportWriting = reportWriting;
        this.reportContent = reportContent;
        this.aiUsagePlan = aiUsagePlan;
        this.creativity = creativity;
        this.userExperience = userExperience;
        this.socialValueCommunity = socialValueCommunity;
        this.aiUtilizationCommunity = aiUtilizationCommunity;
        this.presentationCommunity = presentationCommunity;
        calculateSubTotalScore();
    }

    private void calculateSubTotalScore() {
        this.subTotalScore = safe(this.technicalCompleteness)
                + safe(this.socialValueMajor)
                + safe(this.aiUtilizationMajor)
                + safe(this.presentationMajor)
                + safe(this.reportWriting)
                + safe(this.reportContent)
                + safe(this.aiUsagePlan)
                + safe(this.creativity)
                + safe(this.userExperience)
                + safe(this.socialValueCommunity)
                + safe(this.aiUtilizationCommunity)
                + safe(this.presentationCommunity);
    }

    public void updateMajorScore(Integer technicalCompleteness, Integer socialValueMajor,
                            Integer aiUtilizationMajor, Integer presentationMajor) {
        this.technicalCompleteness = technicalCompleteness;
        this.socialValueMajor = socialValueMajor;
        this.aiUtilizationMajor = aiUtilizationMajor;
        this.presentationMajor = presentationMajor;

        calculateSubTotalScore();
    }

    public void updateReportScore(Integer reportWriting, Integer reportContent,
                                  Integer aiUsagePlan, Integer creativity) {
        this.reportWriting = reportWriting;
        this.reportContent = reportContent;
        this.aiUsagePlan = aiUsagePlan;
        this.creativity = creativity;

        calculateSubTotalScore();
    }

    public void updateSocialScore(Integer userExperience, Integer socialValueCommunity,
                                  Integer aiUtilizationCommunity, Integer presentationCommunity) {
        this.userExperience = userExperience;
        this.socialValueCommunity = socialValueCommunity;
        this.aiUtilizationCommunity = aiUtilizationCommunity;
        this.presentationCommunity = presentationCommunity;

        calculateSubTotalScore();
    }

    private int safe(Integer value) {
        return Objects.requireNonNullElse(value, 0);
    }
}
