package com.example.gifserverv2.domain.student.entity;

import com.example.gifserverv2.domain.project.entity.Project;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 관리자가 미배정 학생을 프로젝트에 추가하려 할 때 생기는 요청. 팀장이 수락해야 팀원이 된다.
 */
@Entity
@Table(name = "team_join_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamJoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "requested_by", nullable = false)
    private Long requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "reject_reason", length = 200)
    private String rejectReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    public enum Status {
        PENDING, ACCEPTED, REJECTED, CANCELLED
    }

    public TeamJoinRequest(Project project, Long userId, Long requestedBy) {
        this.project = project;
        this.userId = userId;
        this.requestedBy = requestedBy;
        this.status = Status.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return status == Status.PENDING;
    }

    public void accept() {
        close(Status.ACCEPTED, null);
    }

    public void reject(String reason) {
        close(Status.REJECTED, reason);
    }

    public void cancel() {
        close(Status.CANCELLED, null);
    }

    private void close(Status status, String rejectReason) {
        this.status = status;
        this.rejectReason = rejectReason;
        this.respondedAt = LocalDateTime.now();
    }
}
