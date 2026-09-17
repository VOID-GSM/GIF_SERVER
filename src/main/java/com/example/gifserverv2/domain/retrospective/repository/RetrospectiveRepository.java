package com.example.gifserverv2.domain.retrospective.repository;

import com.example.gifserverv2.domain.retrospective.entity.Retrospective;
import com.example.gifserverv2.domain.retrospective.entity.RetrospectiveCategory;
import com.example.gifserverv2.domain.retrospective.entity.RetrospectiveVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RetrospectiveRepository extends JpaRepository<Retrospective, Long> {

    List<Retrospective> findAllByIsDraftTrueAndUserIdOrderByUpdatedAtDesc(Long userId);

    @Query("""
            SELECT r FROM Retrospective r
            WHERE r.isDraft = false
              AND (r.visibility = :publicVisibility OR r.userId = :userId)
              AND (:category IS NULL OR r.category = :category)
              AND (:keyword IS NULL OR r.title LIKE CONCAT('%', :keyword, '%') OR r.content LIKE CONCAT('%', :keyword, '%'))
            """)
    Page<Retrospective> search(
            @Param("userId") Long userId,
            @Param("publicVisibility") RetrospectiveVisibility publicVisibility,
            @Param("category") RetrospectiveCategory category,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
