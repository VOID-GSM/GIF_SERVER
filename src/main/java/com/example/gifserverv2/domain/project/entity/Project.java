package com.example.gifserverv2.domain.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
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

    @Column(name = "class_no")
    private Integer classNo;

    @Column(name = "team_no")
    private Integer teamNo;

    @Column(name = "advisor_teacher_id")
    private Long advisorTeacherId;

    @Column(length = 1000)
    private String aiSummary;

    @Column
    private LocalDateTime aiSummarizedAt;

    // 소개(description)의 일본어 AI 번역. descriptionJaSourceHash 가 지금 소개의 해시와 같을 때만 유효하다.
    @Column(length = 2000)
    private String descriptionJa;

    @Column(length = 64)
    private String descriptionJaSourceHash;

    // aiSummary 를 일본어로 번역한 것. aiSummary 가 지워지면 함께 지운다.
    @Column(length = 1000)
    private String aiSummaryJa;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProjectMember> members = new ArrayList<>();

    public void updateName(String name) { this.name = name; }
    public void updateTeamName(String teamName) { this.teamName = teamName; }
    public void updateDescription(String description) { this.description = description; }
    public void updateLogo(String logo) { this.logo = logo; }
    public void updateGrade(Integer grade) { this.grade = grade; }
    public void updateClassNo(Integer classNo) { this.classNo = classNo; }
    public void updateTeamNo(Integer teamNo) { this.teamNo = teamNo; }
    public void assignAdvisorTeacher(Long teacherId) { this.advisorTeacherId = teacherId; }
    public Long getAdvisorTeacherId() { return this.advisorTeacherId; }
    public void updateAiSummary(String aiSummary) {
        this.aiSummary = aiSummary;
        this.aiSummarizedAt = LocalDateTime.now();
        this.aiSummaryJa = null;
    }

    public void clearAiSummary() {
        this.aiSummary = null;
        this.aiSummarizedAt = null;
        this.aiSummaryJa = null;
    }

    public void updateAiSummaryJa(String aiSummaryJa) { this.aiSummaryJa = aiSummaryJa; }

    // 소개가 바뀌면 해시가 달라져 기존 번역은 자동으로 무효가 된다.
    public String currentDescriptionJa() {
        if (descriptionJa == null || description == null) {
            return null;
        }
        return descriptionHash().equals(descriptionJaSourceHash) ? descriptionJa : null;
    }

    public void updateDescriptionJa(String descriptionJa, String sourceHash) {
        this.descriptionJa = descriptionJa;
        this.descriptionJaSourceHash = sourceHash;
    }

    public String descriptionHash() {
        return hash(description);
    }

    private static String hash(String text) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}