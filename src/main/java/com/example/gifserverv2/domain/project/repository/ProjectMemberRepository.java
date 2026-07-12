package com.example.gifserverv2.domain.project.repository;

import com.example.gifserverv2.domain.project.entity.ProjectMember;
import com.example.gifserverv2.domain.user.entity.ClientRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    Optional<ProjectMember> findByProjectIdAndRole(Long projectId, ClientRole role);

    boolean existsByUserIdAndRole(Long userId, ClientRole role);

    List<ProjectMember> findAllByProjectId(Long projectId);

    List<ProjectMember> findAllByUserId(Long userId);

    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    boolean existsByUserId(Long userId);

    List<ProjectMember> findAllByUserIdIn(List<Long> userIds);
}
