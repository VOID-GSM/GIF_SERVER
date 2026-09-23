package com.example.gifserverv2.domain.assessment.roadmaking.repository;

import com.example.gifserverv2.domain.assessment.roadmaking.entity.RoadMakingProblem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoadMakingProblemRepository extends JpaRepository<RoadMakingProblem, Long> {

    Optional<RoadMakingProblem> findByIdAndRoundId(Long id, Long roundId);

    Optional<RoadMakingProblem> findFirstByRoundIdAndSolvedFalseOrderBySequenceAsc(Long roundId);
}