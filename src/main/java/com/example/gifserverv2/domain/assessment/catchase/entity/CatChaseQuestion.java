package com.example.gifserverv2.domain.assessment.catchase.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 고양이 술래잡기 한 문항. 고양이 위치마다 쥐를 잡았는지(FOUND/MISSED) 판단하며, 모두 맞혀야 정답이다.
 */
@Entity
@Table(name = "cat_chase_question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CatChaseQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "round_id", nullable = false)
    private Long roundId;

    @Column(nullable = false)
    private int sequence;

    @ElementCollection
    @CollectionTable(name = "cat_chase_question_mouse_position", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "position", nullable = false)
    @OrderColumn(name = "idx")
    private List<Integer> mousePositions = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "cat_chase_question_cat_position", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "position", nullable = false)
    @OrderColumn(name = "idx")
    private List<Integer> catPositions = new ArrayList<>();

    @Column(nullable = false)
    private boolean answered;

    @Column(nullable = false)
    private boolean correct;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime answeredAt;

    private CatChaseQuestion(Long roundId, int sequence, List<Integer> mousePositions, List<Integer> catPositions) {
        this.roundId = roundId;
        this.sequence = sequence;
        this.mousePositions = new ArrayList<>(mousePositions);
        this.catPositions = new ArrayList<>(catPositions);
    }

    public static CatChaseQuestion issue(Long roundId, int sequence, List<Integer> mousePositions, List<Integer> catPositions) {
        return new CatChaseQuestion(roundId, sequence, mousePositions, catPositions);
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public boolean hasMouseAt(int position) {
        return mousePositions.contains(position);
    }

    public void answer(boolean correct, LocalDateTime now) {
        this.answered = true;
        this.correct = correct;
        this.answeredAt = now;
    }
}
