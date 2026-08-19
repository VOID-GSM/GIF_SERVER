package com.example.gifserverv2.domain.user.entity;

import com.example.gifserverv2.domain.project.entity.Project;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_teacher_assignment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProjectTeacherAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private UserEntity teacher;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status;

    @Column(length = 500)
    private String rejectReason;

    public enum AssignmentStatus {
        PENDING, ACCEPTED, REJECTED
    }

    public void accept() {
        this.status = AssignmentStatus.ACCEPTED;
        this.rejectReason = null;
    }

    public void reject(String reason) {
        this.status = AssignmentStatus.REJECTED;
        this.rejectReason = reason;
    }
}
