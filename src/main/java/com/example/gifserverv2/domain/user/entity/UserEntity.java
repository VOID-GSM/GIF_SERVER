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

    @Column(name = "student_number", nullable = false, length = 10)
    private String studentNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "admin_role")
    private AdminRole adminRole;

    @Column(name = "admin_team")
    private String adminTeam;

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

    public Role getRole() {
        return role;
    }

    public AdminRole getAdminRole() {
        return adminRole;
    }

    public String getAdminTeam() {
        return adminTeam;
    }

    public ClientRole getClientRole() {
        return clientRole;
    }

    public Role getEffectiveRole() {
        if (this.adminRole != null) {
            return Role.ADMIN;
        }
        if (this.clientRole != null) {
            return Role.CLIENT;
        }
        return this.role;
    }

    public void updateProfile(String name, String studentNumber) {
        this.name = name;
        this.studentNumber = studentNumber;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void updateAdminAdditionalInfo(AdminRole adminRole, String adminTeam) {
        this.adminRole = adminRole;
        this.adminTeam = adminTeam;
        if (adminRole != null) {
            this.role = Role.ADMIN;
        } else if (this.clientRole != null) {
            this.role = Role.CLIENT;
        } else {
            this.role = Role.USER;
        }
    }

    public void updateClientAdditionalInfo(ClientRole clientRole) {
        this.clientRole = clientRole;
        if (clientRole != null) {
            this.role = Role.CLIENT;
        } else if (this.adminRole != null) {
            this.role = Role.ADMIN;
        } else {
            this.role = Role.USER;
        }
    }
}