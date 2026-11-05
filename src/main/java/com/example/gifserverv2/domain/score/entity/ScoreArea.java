package com.example.gifserverv2.domain.score.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "score_area", uniqueConstraints = @UniqueConstraint(name = "uk_score_area_name", columnNames = "name"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScoreArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @OneToMany(mappedBy = "area", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScoreItem> items = new ArrayList<>();

    @Builder
    public ScoreArea(String name, int displayOrder) {
        this.name = name;
        this.displayOrder = displayOrder;
    }

    public void update(String name, Integer displayOrder) {
        this.name = name != null ? name : this.name;
        this.displayOrder = displayOrder != null ? displayOrder : this.displayOrder;
    }
}
