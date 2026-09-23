package com.example.gifserverv2.domain.assessment.roadmaking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "road_making_problem")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoadMakingProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "round_id", nullable = false)
    private RoadMakingRound round;

    @Column(nullable = false)
    private int sequence;

    @Column(nullable = false)
    private int minFenceCount;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String boardJson;

    @Column(nullable = false)
    private boolean solved;

    @Column(nullable = false)
    private int attemptCount;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    private LocalDateTime solvedAt;

    private RoadMakingProblem(RoadMakingRound round, int sequence, int minFenceCount,
                              String boardJson, LocalDateTime issuedAt) {
        this.round = round;
        this.sequence = sequence;
        this.minFenceCount = minFenceCount;
        this.boardJson = boardJson;
        this.issuedAt = issuedAt;
    }

    public static RoadMakingProblem issue(RoadMakingRound round, int sequence, int minFenceCount,
                                          String boardJson, LocalDateTime now) {
        return new RoadMakingProblem(round, sequence, minFenceCount, boardJson, now);
    }

    public void increaseAttempt() {
        this.attemptCount++;
    }

    public void markSolved(LocalDateTime now) {
        this.solved = true;
        this.solvedAt = now;
    }
}