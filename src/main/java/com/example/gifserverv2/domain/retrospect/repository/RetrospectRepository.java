package com.example.gifserverv2.domain.retrospect.repository;

import com.example.gifserverv2.domain.retrospect.entity.Retrospect;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RetrospectRepository extends JpaRepository<Retrospect, Long> {
    Optional<Retrospect> findByProjectIdAndUserId(Long projectId, Long userId);
    List<Retrospect> findAllByProjectIdOrderByUpdatedAtDesc(Long projectId);
}