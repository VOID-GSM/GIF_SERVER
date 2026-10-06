package com.example.gifserverv2.domain.user.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(name = "student_number", nullable = true, length = 10)
    private String studentNumber;

    @Column(name = "student_grade")
    private String grade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "admin_role")
    private AdminRole adminRole;

    @Column(name = "admin_team")
    private String adminTeam;

    @Column(name = "grade_head", nullable = false, columnDefinition = "boolean default false")
    private boolean gradeHead = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_role")
    private ClientRole clientRole;

    @Column(name = "github_username", nullable = true)
    private String githubUsername;

    @Column(name = "github_avatar_url")
    private String githubAvatarUrl;

    @Column(name = "github_access_token")
    private String githubAccessToken;

    @Column(name = "initial_commit_count")
    private Integer initialCommitCount = 0;

    @Column(name = "coin_balance", nullable = false, columnDefinition = "int default 0")
    private int coinBalance = 0;

    protected UserEntity() {
    }

    public UserEntity(String email, String name, String studentNumber, Role role) {
        this.email = email;
        this.name = name;
        this.studentNumber = studentNumber;
        this.role = role;
    }

    public UserEntity(String email, String name, String studentNumber, Role role, String grade) {
        this.email = email;
        this.name = name;
        this.studentNumber = studentNumber;
        this.role = role;
        this.grade = grade;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public String getGrade() {
        return grade;
    }

    public Role getRole() {
        return role;
    }

    public AdminRole getAdminRole() {
        return adminRole;
    }

    public String getAdminTeam() {
        return adminTeam;
    }

    public boolean isGradeHead() {
        return gradeHead;
    }

    public ClientRole getClientRole() {
        return clientRole;
    }

    public String getGithubUsername() {
        return githubUsername;
    }

    public String getGithubAvatarUrl() {
        return githubAvatarUrl;
    }

    public String getGithubAccessToken() {
        return githubAccessToken;
    }

    public Integer getInitialCommitCount() {
        return initialCommitCount != null ? initialCommitCount : 0;
    }

    public int getCoinBalance() {
        return coinBalance;
    }

    public Role getEffectiveRole() {
        if (this.adminRole != null) {
            return Role.ADMIN;
        }
        if (this.clientRole != null) {
            return Role.USER;
        }
        return this.role;
    }

    public void updateProfile(String name, String studentNumber) {
        updateProfile(name, studentNumber, this.grade);
    }

    public void updateProfile(String name, String studentNumber, String grade) {
        this.name = name;
        this.studentNumber = studentNumber;
        this.grade = grade;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void updateAdminAdditionalInfo(AdminRole adminRole, String name, String adminTeam, boolean gradeHead) {
        this.adminRole = adminRole;
        this.name = name;
        this.adminTeam = adminTeam;
        this.gradeHead = gradeHead;
        if (adminRole != null) {
            this.role = Role.ADMIN;
        } else if (this.clientRole != null) {
            this.role = Role.USER;
        } else {
            this.role = Role.USER;
        }
    }

    public void updateClientAdditionalInfo(ClientRole clientRole) {
        this.clientRole = clientRole;
        if (clientRole != null) {
            this.role = Role.USER;
        } else if (this.adminRole != null) {
            this.role = Role.ADMIN;
        } else {
            this.role = Role.USER;
        }
    }

    public void updateAdminTeam(String adminTeam) {
        this.adminTeam = adminTeam;
    }

    public void updateGithubUsername(String githubUsername) {
        this.githubUsername = githubUsername;
    }

    public void updateGithubInfo(String githubUsername, String githubAvatarUrl) {
        this.githubUsername = githubUsername;
        this.githubAvatarUrl = githubAvatarUrl;
    }

    public void connectGithub(String githubUsername, String githubAvatarUrl, String githubAccessToken, int initialCommitCount) {
        this.githubUsername = githubUsername;
        this.githubAvatarUrl = githubAvatarUrl;
        this.githubAccessToken = githubAccessToken;
        this.initialCommitCount = initialCommitCount;
    }

    public void disconnectGithub() {
        this.githubUsername = null;
        this.githubAvatarUrl = null;
        this.githubAccessToken = null;
        this.initialCommitCount = 0;
    }

    public void updateCoinBalance(int newCoinBalance) {
        this.coinBalance = Math.max(0, newCoinBalance);
    }
}