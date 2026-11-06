package com.example.gifserverv2.domain.score.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 선생님은 options(예: 40/32/24) 중 하나를 골라 채점한다. maxScore 는 가장 높은 선택지로 자동 계산된다.
 * 삭제 방식은 {@link ScoreArea} 와 같다.
 */
@Entity
@Table(name = "score_item", uniqueConstraints = @UniqueConstraint(name = "uk_score_item_area_name", columnNames = {"area_id", "name", "delete_token"}))
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

    /** 높은 점수부터 저장한다. */
    @ElementCollection
    @CollectionTable(name = "score_item_option", joinColumns = @JoinColumn(name = "score_item_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "score", nullable = false)
    @BatchSize(size = 100)
    private List<Integer> options = new ArrayList<>();

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @ColumnDefault("0")
    @Column(name = "delete_token", nullable = false)
    private long deleteToken = 0L;

    @Builder
    public ScoreItem(ScoreArea area, String name, String criteria, List<Integer> options, int displayOrder) {
        this.area = area;
        this.name = name;
        this.criteria = criteria;
        this.displayOrder = displayOrder;
        changeOptions(options);
    }

    public void update(String name, List<Integer> options, Integer displayOrder) {
        this.name = name != null ? name : this.name;
        this.displayOrder = displayOrder != null ? displayOrder : this.displayOrder;
        if (options != null) {
            changeOptions(options);
        }
    }

    public void updateCriteria(String criteria) {
        this.criteria = criteria;
    }

    public boolean isSelectable(int score) {
        return options.contains(score);
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
        this.deleteToken = this.id;
    }

    private void changeOptions(List<Integer> options) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("선택지 점수는 1개 이상이어야 합니다.");
        }
        List<Integer> sorted = options.stream().distinct().sorted(Comparator.reverseOrder()).toList();
        this.options.clear();
        this.options.addAll(sorted);
        this.maxScore = sorted.get(0);
    }
}
