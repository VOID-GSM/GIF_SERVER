package com.example.gifserverv2.domain.assessment.catchase.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cat_chase_round")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CatChaseRound {

    public static final int GRID_SIZE = 36;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ElementCollection
    @CollectionTable(name = "cat_chase_mouse_position", joinColumns = @JoinColumn(name = "round_id"))
    @Column(name = "position", nullable = false)
    @Builder.Default
    private List<Integer> mousePositions = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "cat_chase_cat_position", joinColumns = @JoinColumn(name = "round_id"))
    @Column(name = "position", nullable = false)
    @Builder.Default
    private List<Integer> catPositions = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }
}
