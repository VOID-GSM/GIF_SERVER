package com.example.gifserverv2.domain.assessment.catchase.repository;

import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseJudgment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatChaseJudgmentRepository extends JpaRepository<CatChaseJudgment, Long> {
    List<CatChaseJudgment> findAllByRoundId(Long roundId);
    boolean existsByRoundId(Long roundId);
}
