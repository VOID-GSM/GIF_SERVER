package com.example.gifserverv2.domain.user.repository;

import com.example.gifserverv2.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByAdminTeamAndIdNotAndAdminRoleIsNotNull(String adminTeam, Long id);

}
