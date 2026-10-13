package com.example.gifserverv2.domain.engagement.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** 사용자별 마지막 조회 시각. 조회수 중복 집계 방지에 사용한다. */
@Entity
@Table(
        name = "project_view",
        uniqueConstraints = @UniqueConstraint(name = "uk_project_view_project_user", columnNames = {"project_id", "user_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "last_viewed_at", nullable = false)
    private Instant lastViewedAt;

    public ProjectView(Long projectId, Long userId, Instant lastViewedAt) {
        this.projectId = projectId;
        this.userId = userId;
        this.lastViewedAt = lastViewedAt;
    }
}
