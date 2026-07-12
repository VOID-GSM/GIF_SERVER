package com.example.gifserverv2.domain.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, length = 20)
    private String teamName;

    @Column(length = 500)
    private String description;

    @Column
    private String logo;

    @Column
    private Integer grade;

    @Column(name = "advisor_teacher_id")
    private Long advisorTeacherId;

    @Column(length = 1000)
    private String aiSummary;

    @Column
    private LocalDateTime aiSummarizedAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProjectMember> members = new ArrayList<>();

    public void updateName(String name) { this.name = name; }
    public void updateTeamName(String teamName) { this.teamName = teamName; }
    public void updateDescription(String description) { this.description = description; this.clearAiSummary(); }
    public void updateLogo(String logo) { this.logo = logo; }
    public void updateGrade(Integer grade) { this.grade = grade; }
    public void assignAdvisorTeacher(Long teacherId) { this.advisorTeacherId = teacherId; }
    public Long getAdvisorTeacherId() { return this.advisorTeacherId; }

    public void updateAiSummary(String aiSummary) {
        this.aiSummary = aiSummary;
        this.aiSummarizedAt = LocalDateTime.now();
    }

    public void clearAiSummary() {
        this.aiSummary = null;
        this.aiSummarizedAt = null;
    }
}