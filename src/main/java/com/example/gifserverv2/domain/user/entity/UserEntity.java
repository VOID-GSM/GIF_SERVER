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
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
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
}
