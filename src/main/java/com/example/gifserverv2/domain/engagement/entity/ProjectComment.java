package com.example.gifserverv2.domain.engagement.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "project_comment", indexes = @Index(name = "idx_project_comment_project", columnList = "project_id, created_at"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 작성 시점의 작성자 이름 스냅샷 */
    @Column(name = "author_name", nullable = false, length = 50)
    private String authorName;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** 관리자가 숨긴 댓글 여부 */
    @Column(name = "hidden", nullable = false)
    private boolean hidden = false;

    /** 삭제된 시각. null 이면 삭제되지 않은 댓글 */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    public ProjectComment(Long projectId, Long userId, String authorName, String content) {
        this.projectId = projectId;
        this.userId = userId;
        this.authorName = authorName;
        this.content = content;
        this.createdAt = Instant.now();
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }

    public void hide() { this.hidden = true; }
    public void unhide() { this.hidden = false; }
    public void softDelete() { this.deletedAt = Instant.now(); }
    public boolean isDeleted() { return this.deletedAt != null; }
}
