package com.example.gifserverv2.domain.score.repository;

import com.example.gifserverv2.domain.score.entity.EvaluationPeriod;
import com.example.gifserverv2.domain.score.entity.ScoreCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationPeriodRepository extends JpaRepository<EvaluationPeriod, ScoreCategory> {
}