package com.example.gifserverv2.domain.assessment.catchase.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cat_chase_judgment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CatChaseJudgment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "round_id", nullable = false)
    private Long roundId;

    @Column(nullable = false)
    private int position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Judgment judgment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConfidenceLevel confidence;

    @Column(name = "had_mouse", nullable = false)
    private boolean hadMouse;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
