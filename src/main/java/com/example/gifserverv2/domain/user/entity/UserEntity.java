package com.example.gifserverv2.domain.user.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    // MySQL 에서 EnumType.STRING 은 기본으로 enum(...) 컬럼이 되어 enum 값을 추가하면 저장이 실패한다. VARCHAR 로 고정한다.
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "admin_role", length = 20)
    private AdminRole adminRole;

    @Column(name = "admin_team")
    private String adminTeam;

    @Column(name = "grade_head", nullable = false, columnDefinition = "boolean default false")
    private boolean gradeHead = false;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "client_role", length = 20)
    private ClientRole clientRole;

    @Column(name = "github_username", nullable = true)
    private String githubUsername;

    @Column(name = "github_avatar_url")
    private String githubAvatarUrl;

    @Column(name = "github_access_token")
    private String githubAccessToken;

    /** GitHub 을 연동한 시각. 이 시각 이후의 푸시·커밋만 코인으로 지급한다 */
    @Column(name = "github_connected_at")
    private Instant githubConnectedAt;

    @Column(name = "coin_balance", nullable = false, columnDefinition = "int default 0")
    private int coinBalance = 0;

    @Column(name = "major", length = 50)
    private String major;

    /** 직접 업로드한 프로필 이미지. 없으면 GitHub 아바타를 보여준다. */
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @ElementCollection
    @CollectionTable(name = "user_tech_stack", joinColumns = @JoinColumn(name = "user_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "name", nullable = false, length = 30)
    private List<String> techStacks = new ArrayList<>();

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

    public Instant getGithubConnectedAt() {
        return githubConnectedAt;
    }

    public int getCoinBalance() {
        return coinBalance;
    }

    public String getMajor() {
        return major;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    /** 업로드한 이미지가 있으면 그것을, 없으면 GitHub 아바타를 반환한다. */
    public String getDisplayProfileImageUrl() {
        return profileImageUrl != null ? profileImageUrl : githubAvatarUrl;
    }

    public List<String> getTechStacks() {
        return Collections.unmodifiableList(techStacks);
    }

    public void updateMajor(String major) {
        this.major = major;
    }

    public void replaceTechStacks(List<String> techStacks) {
        this.techStacks.clear();
        this.techStacks.addAll(techStacks);
    }

    public void changeProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
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

    public void connectGithub(String githubUsername, String githubAvatarUrl, String githubAccessToken, Instant connectedAt) {
        this.githubUsername = githubUsername;
        this.githubAvatarUrl = githubAvatarUrl;
        this.githubAccessToken = githubAccessToken;
        this.githubConnectedAt = connectedAt;
    }

    public void markGithubConnectedAt(Instant connectedAt) {
        this.githubConnectedAt = connectedAt;
    }

    public void disconnectGithub() {
        this.githubUsername = null;
        this.githubAvatarUrl = null;
        this.githubAccessToken = null;
        this.githubConnectedAt = null;
    }

    public void updateCoinBalance(int newCoinBalance) {
        this.coinBalance = Math.max(0, newCoinBalance);
    }
}