package com.example.gifserverv2.domain.score.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EvaluationPeriod {

    @Id
    @Enumerated(EnumType.STRING)
    private ScoreCategory category;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    public EvaluationPeriod(ScoreCategory category, LocalDateTime startDate, LocalDateTime endDate) {
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void updatePeriod(LocalDateTime startDate, LocalDateTime endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public boolean isWithinPeriod(LocalDateTime now) {
        return (now.isEqual(startDate) || now.isAfter(startDate)) &&
                (now.isEqual(endDate) || now.isBefore(endDate));
    }
}