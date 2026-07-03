package com.example.gifserverv2.domain.project.repository;

import com.example.gifserverv2.domain.project.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findAllByProjectId(Long projectId);

    List<TeamMember> findAllByUserId(Long userId);

    Optional<TeamMember> findByProjectIdAndUserId(Long projectId, Long userId);

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);
}
