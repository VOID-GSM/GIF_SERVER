package com.example.gifserverv2.domain.project.entity;

import com.example.gifserverv2.domain.user.entity.UserEntity;
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

    @Column(length = 200)
    private String rejectReason;

    public enum AssignmentStatus {
        PENDING, ACCEPTED, REJECTED
    }

    public void accept() {
        this.status = AssignmentStatus.ACCEPTED;
        this.rejectReason = null;
    }

    public void reject(String reason) {
        if (reason != null && reason.length() > 200) {
            throw new IllegalArgumentException("거절 사유는 200자를 초과할 수 없습니다.");
        }
        this.status = AssignmentStatus.REJECTED;
        this.rejectReason = reason;
    }
}