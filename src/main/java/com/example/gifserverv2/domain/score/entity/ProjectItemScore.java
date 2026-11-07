package com.example.gifserverv2.domain.score.entity;

import com.example.gifserverv2.domain.project.entity.Project;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 평가 항목 기반 채점 기록. 선생님(evaluator) 한 명이 프로젝트의 항목마다 점수 하나를 가진다.
 * 항목이 삭제되거나 선택지가 바뀌어도 이미 기록된 점수는 바꾸지 않는다.
 */
@Entity
@Table(name = "project_item_score",
        uniqueConstraints = @UniqueConstraint(name = "uk_project_item_score",
                columnNames = {"project_id", "evaluator_id", "score_item_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectItemScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "evaluator_id", nullable = false)
    private Long evaluatorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "score_item_id", nullable = false)
    private ScoreItem item;

    @Column(nullable = false)
    private int score;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ProjectItemScore(Project project, Long evaluatorId, ScoreItem item, int score) {
        this.project = project;
        this.evaluatorId = evaluatorId;
        this.item = item;
        this.score = score;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void changeScore(int score) {
        if (this.score != score) {
            this.score = score;
            this.updatedAt = Instant.now();
        }
    }
}
