package com.example.gifserverv2.domain.assessment.catchase.repository;

import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseRound;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface CatChaseRoundRepository extends JpaRepository<CatChaseRound, Long> {
    List<CatChaseRound> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CatChaseRound> findByIdAndUserId(Long id, Long userId);

    List<CatChaseRound> findAllByCompletedTrueAndQuestionCountGreaterThan(int questionCount);
}
