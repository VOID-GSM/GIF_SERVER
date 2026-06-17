package com.example.gifserverv2.domain.score.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "score_notice")
public class ScoreNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private boolean published;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Lob
    @Column(name = "snapshot", columnDefinition = "TEXT")
    private String snapshot;

    @Column(name = "created_by")
    private Long createdBy;

    protected ScoreNotice() {
    }

    public ScoreNotice(boolean published, Instant publishedAt, String snapshot, Long createdBy) {
        this.published = published;
        this.publishedAt = publishedAt;
        this.snapshot = snapshot;
        this.createdBy = createdBy;
    }

    public Long getId() { return id; }
    public boolean isPublished() { return published; }
    public Instant getPublishedAt() { return publishedAt; }
    public String getSnapshot() { return snapshot; }
    public Long getCreatedBy() { return createdBy; }
}
