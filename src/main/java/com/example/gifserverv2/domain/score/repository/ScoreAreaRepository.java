package com.example.gifserverv2.domain.score.repository;

import com.example.gifserverv2.domain.score.entity.ScoreArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ScoreAreaRepository extends JpaRepository<ScoreArea, Long> {

    Optional<ScoreArea> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByNameAndDeletedAtIsNull(String name);

    boolean existsByNameAndDeletedAtIsNullAndIdNot(String name, Long id);

    // 삭제된 항목은 ScoreArea.items 의 @SQLRestriction 으로 빠진다.
    @Query("SELECT DISTINCT a FROM ScoreArea a LEFT JOIN FETCH a.items WHERE a.deletedAt IS NULL ORDER BY a.displayOrder, a.id")
    List<ScoreArea> findAllWithItems();

    @Query("SELECT COALESCE(MAX(a.displayOrder), -1) + 1 FROM ScoreArea a WHERE a.deletedAt IS NULL")
    int findNextDisplayOrder();
}
