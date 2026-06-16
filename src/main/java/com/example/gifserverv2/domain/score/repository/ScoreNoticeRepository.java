package com.example.gifserverv2.domain.score.repository;

import com.example.gifserverv2.domain.score.entity.ScoreNotice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScoreNoticeRepository extends JpaRepository<ScoreNotice, Long> {
    Optional<ScoreNotice> findTopByOrderByPublishedAtDesc();
}
