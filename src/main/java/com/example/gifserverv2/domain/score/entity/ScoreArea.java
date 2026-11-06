package com.example.gifserverv2.domain.score.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 삭제는 행을 지우지 않고 deleted_at 을 기록한다. (채점 기록이 항목을 참조하므로)
 * delete_token 은 살아 있는 행이면 0, 삭제된 행이면 자신의 id 라서 이름 유니크 제약이 살아 있는 영역끼리만 걸린다.
 */
@Entity
@Table(name = "score_area", uniqueConstraints = @UniqueConstraint(name = "uk_score_area_name", columnNames = {"name", "delete_token"}))
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

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @ColumnDefault("0")
    @Column(name = "delete_token", nullable = false)
    private long deleteToken = 0L;

    @OneToMany(mappedBy = "area", cascade = CascadeType.ALL)
    @SQLRestriction("deleted_at IS NULL")
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

    public void softDelete() {
        items.forEach(ScoreItem::softDelete);
        this.deletedAt = Instant.now();
        this.deleteToken = this.id;
    }
}
