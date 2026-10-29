package com.example.gifserverv2.domain.engagement.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "project_like",
        uniqueConstraints = @UniqueConstraint(name = "uk_project_like_project_user", columnNames = {"project_id", "user_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public ProjectLike(Long projectId, Long userId) {
        this.projectId = projectId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }
}
