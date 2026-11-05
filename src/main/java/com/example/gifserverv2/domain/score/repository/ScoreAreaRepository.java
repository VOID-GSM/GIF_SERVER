package com.example.gifserverv2.domain.score.repository;

import com.example.gifserverv2.domain.score.entity.ScoreArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ScoreAreaRepository extends JpaRepository<ScoreArea, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    @Query("SELECT DISTINCT a FROM ScoreArea a LEFT JOIN FETCH a.items ORDER BY a.displayOrder, a.id")
    List<ScoreArea> findAllWithItems();

    @Query("SELECT COALESCE(MAX(a.displayOrder), -1) + 1 FROM ScoreArea a")
    int findNextDisplayOrder();
}
