package com.example.gifserverv2.domain.project.entity;

import jakarta.persistence.*;
import lombok.*;

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
    private String logoPath;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProjectMember> members = new ArrayList<>();

    public void updateName(String name) { this.name = name; }
    public void updateTeamName(String teamName) { this.teamName = teamName; }
    public void updateDescription(String description) { this.description = description; }
    public void updateLogoPath(String logoPath) { this.logoPath = logoPath; }
    public void deleteLogo() { this.logoPath = null; }
}
