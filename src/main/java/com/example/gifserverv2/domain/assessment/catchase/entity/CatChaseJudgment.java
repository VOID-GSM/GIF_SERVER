package com.example.gifserverv2.domain.assessment.catchase.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    // 문항 도입 이전 판단 기록은 null
    @Column(name = "question_id")
    private Long questionId;

    @Column(nullable = false)
    private int position;

    // MySQL enum(...) 컬럼이 되면 enum 값 추가 시 저장이 실패하므로 VARCHAR 로 고정한다
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private Judgment judgment;

    // TIMEOUT 판단은 null
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 20)
    private ConfidenceLevel confidence;

    @Column(name = "had_mouse", nullable = false)
    private boolean hadMouse;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    /** 쥐가 있었으면 FOUND, 없었으면 MISSED 가 정답. TIMEOUT 은 항상 오답 */
    public boolean isCorrect() {
        if (judgment == Judgment.TIMEOUT) {
            return false;
        }
        return (judgment == Judgment.FOUND) == hadMouse;
    }
}
