package com.example.gifserverv2.domain.project.repository;

import com.example.gifserverv2.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSearchRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByAdminTeamAndIdNotAndAdminRoleIsNotNull(String adminTeam, Long id);

    List<UserEntity> findByNameContainingOrStudentNumberContaining(String name, String studentNumber);
}