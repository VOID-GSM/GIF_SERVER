package com.example.gifserverv2.domain.project.entity;

import com.example.gifserverv2.domain.user.entity.ClientRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProjectMember {

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
    private ClientRole role;

    public void changeRole(ClientRole role){
        this.role = role;
    }
}