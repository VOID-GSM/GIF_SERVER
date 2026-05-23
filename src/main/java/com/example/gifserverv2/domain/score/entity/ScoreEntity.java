package com.example.gifserverv2.domain.score.entity;

import com.example.gifserverv2.domain.project.entity.Project;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "evaluator_id", nullable = false)
    private String evaluatorId;

    @Column(name = "technical_score", nullable = false)
    private Integer technicalScore;

    @Column(name = "social_value_score", nullable = false)
    private Integer socialValueScore;

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

    @Column(name = "sub_total_score", nullable = false)
    private Integer subTotalScore;

    @Column(name = "user_experience", nullable = false)
    private Integer userExperience;

    @Column(name = "social_value_community", nullable = false)
    private Integer socialValueCommunity;

    @Column(name = "ai_utilization_community", nullable = false)
    private Integer aiUtilizationCommunity;

    @Column(name = "presentation_community", nullable = false)
    private Integer presentationCommunity;

    @Column(name = "score_rank")
    private int rank;

    @Builder
    public ScoreEntity (Project project, String evaluatorId, Integer technicalScore, Integer socialValueScore, Integer aiUtilizationMajor, Integer presentationMajor, Integer reportWriting, Integer reportContent, Integer aiUsagePlan, Integer creativity, Integer userExperience, Integer socialValueCommunity, Integer aiUtilizationCommunity, Integer presentationCommunity) {
        this.project = project;
        this.evaluatorId = evaluatorId;
        this.technicalScore = technicalScore;
        this.socialValueScore = socialValueScore;
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

    public void updateScore(Integer technicalScore, Integer socialValueScore, Integer aiUtilizationMajor, Integer presentationMajor, Integer reportWriting, Integer reportContent, Integer aiUsagePlan, Integer creativity, Integer userExperience, Integer socialValueCommunity, Integer aiUtilizationCommunity, Integer presentationCommunity) {
        this.technicalScore = technicalScore;
        this.socialValueScore = socialValueScore;
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
        this.subTotalScore = this.technicalScore + this.socialValueScore + this.aiUtilizationMajor + this.presentationMajor + this.reportWriting + this.reportContent + this.aiUsagePlan + this.creativity + this.userExperience + this.socialValueCommunity + this.aiUtilizationCommunity + this.presentationCommunity;
    }
}
