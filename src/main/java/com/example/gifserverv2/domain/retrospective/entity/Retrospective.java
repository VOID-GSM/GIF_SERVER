package com.example.gifserverv2.domain.retrospective.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "retrospective_post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Retrospective {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(length = 100)
    private String title;

    @Column(length = 200)
    private String summary;

    @Lob
    @Column(length = 20000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private RetrospectiveCategory category;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private RetrospectiveVisibility visibility;

    @Column(name = "is_draft", nullable = false)
    private boolean isDraft;

    @Column(name = "bookmark_count", nullable = false)
    @Builder.Default
    private long bookmarkCount = 0L;

    @ElementCollection
    @CollectionTable(name = "retrospective_related_work", joinColumns = @JoinColumn(name = "retrospective_id"))
    @Builder.Default
    private List<RetrospectiveRelatedWork> relatedWorks = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void applyPatch(
            String title,
            String content,
            RetrospectiveCategory category,
            RetrospectiveVisibility visibility,
            List<RetrospectiveRelatedWork> relatedWorks,
            String summary
    ) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        if (category != null) this.category = category;
        if (visibility != null) this.visibility = visibility;
        if (relatedWorks != null) {
            this.relatedWorks.clear();
            this.relatedWorks.addAll(relatedWorks);
        }
        if (summary != null) this.summary = summary;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }

    public boolean isPublicPost() {
        return this.visibility == RetrospectiveVisibility.PUBLIC;
    }

    public void increaseBookmarkCount() {
        this.bookmarkCount++;
    }

    public void decreaseBookmarkCount() {
        if (this.bookmarkCount > 0) {
            this.bookmarkCount--;
        }
    }
}
