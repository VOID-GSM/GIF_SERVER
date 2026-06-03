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

    public Role getEffectiveRole() {
        if (role == Role.USER) {
            return Role.CLIENT;
        }

        return role;
    }

    public AdminRole getAdminRole() {
        return adminRole;
    }

    public ClientRole getClientRole() {
        return clientRole;
    }

    public void updateProfile(String name, String studentNumber) {
        this.name = name;
        this.studentNumber = studentNumber;
    }

    public void updateAdminAdditionalInfo(AdminRole adminRole) {
        this.adminRole = adminRole;
        this.clientRole = null;
    }

    public void updateClientAdditionalInfo(ClientRole clientRole) {
        this.clientRole = clientRole;
        this.adminRole = null;
    }
}
