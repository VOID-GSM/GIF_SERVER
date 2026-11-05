package com.example.gifserverv2.domain.score.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "score_item", uniqueConstraints = @UniqueConstraint(name = "uk_score_item_area_name", columnNames = {"area_id", "name"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScoreItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private ScoreArea area;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 1000)
    private String criteria;

    @Column(name = "max_score", nullable = false)
    private int maxScore;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Builder
    public ScoreItem(ScoreArea area, String name, String criteria, int maxScore, int displayOrder) {
        this.area = area;
        this.name = name;
        this.criteria = criteria;
        this.maxScore = maxScore;
        this.displayOrder = displayOrder;
    }

    public void update(String name, Integer maxScore, Integer displayOrder) {
        this.name = name != null ? name : this.name;
        this.maxScore = maxScore != null ? maxScore : this.maxScore;
        this.displayOrder = displayOrder != null ? displayOrder : this.displayOrder;
    }

    public void updateCriteria(String criteria) {
        this.criteria = criteria;
    }
}
