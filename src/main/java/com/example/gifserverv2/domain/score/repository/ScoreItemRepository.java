package com.example.gifserverv2.domain.score.repository;

import com.example.gifserverv2.domain.score.entity.ScoreItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ScoreItemRepository extends JpaRepository<ScoreItem, Long> {

    Optional<ScoreItem> findByIdAndAreaId(Long id, Long areaId);

    boolean existsByAreaIdAndName(Long areaId, String name);

    boolean existsByAreaIdAndNameAndIdNot(Long areaId, String name, Long id);

    @Query("SELECT COALESCE(MAX(i.displayOrder), -1) + 1 FROM ScoreItem i WHERE i.area.id = :areaId")
    int findNextDisplayOrder(@Param("areaId") Long areaId);
}
