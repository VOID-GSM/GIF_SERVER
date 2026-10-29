package com.example.gifserverv2.domain.assessment.roadmaking.repository;

import com.example.gifserverv2.domain.assessment.roadmaking.entity.RoadMakingRound;
import com.example.gifserverv2.domain.assessment.roadmaking.entity.RoundStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface RoadMakingRoundRepository extends JpaRepository<RoadMakingRound, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RoadMakingRound> findByIdAndUserId(Long id, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RoadMakingRound> findFirstByUserIdAndStatus(Long userId, RoundStatus status);
}