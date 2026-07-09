package com.example.gifserverv2.domain.project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "advisor_assignment_request")
public class AdvisorAssignmentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "requested_by", nullable = false)
    private Long requestedBy;

    @Column(name = "advisor_user_id", nullable = false)
    private Long advisorUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum Status {
        PENDING, ACCEPTED, CANCELLED
    }

    protected AdvisorAssignmentRequest() {}

    public AdvisorAssignmentRequest(Long projectId, Long requestedBy, Long advisorUserId) {
        this.projectId = projectId;
        this.requestedBy = requestedBy;
        this.advisorUserId = advisorUserId;
        this.status = Status.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getProjectId() { return projectId; }
    public Long getRequestedBy() { return requestedBy; }
    public Long getAdvisorUserId() { return advisorUserId; }
    public Status getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void accept() {
        this.status = Status.ACCEPTED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = Status.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
}
