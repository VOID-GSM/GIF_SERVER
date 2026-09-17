package com.example.gifserverv2.domain.assessment.catchase.repository;

import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseRound;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatChaseRoundRepository extends JpaRepository<CatChaseRound, Long> {
    List<CatChaseRound> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
