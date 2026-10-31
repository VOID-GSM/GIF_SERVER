package com.example.gifserverv2.domain.assessment.catchase.repository;

import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CatChaseQuestionRepository extends JpaRepository<CatChaseQuestion, Long> {
    Optional<CatChaseQuestion> findByIdAndRoundId(Long id, Long roundId);

    Optional<CatChaseQuestion> findFirstByRoundIdOrderBySequenceDesc(Long roundId);

    List<CatChaseQuestion> findAllByRoundIdOrderBySequenceAsc(Long roundId);
}
