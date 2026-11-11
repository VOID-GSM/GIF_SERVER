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

    List<CatChaseRound> findAllByCompletedTrueAndQuestionCount(int questionCount);

    // 문항 수 도입 이전 라운드(questionCount = 0)는 finishedAt 이 없어도 진행 중이 아니므로 뺀다
    Optional<CatChaseRound> findFirstByUserIdAndFinishedAtIsNullAndQuestionCountGreaterThanOrderByCreatedAtDesc(
            Long userId, int questionCount);
}
