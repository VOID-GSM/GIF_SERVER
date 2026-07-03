package com.example.gifserverv2.domain.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    public enum MemberRole {
        LEADER, MEMBER
    }

    public void changeRole(MemberRole role){
        this.role = role;
    }
}