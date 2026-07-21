package com.example.gifserverv2.domain.user.repository;

import com.example.gifserverv2.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByAdminTeamAndIdNotAndAdminRoleIsNotNull(String adminTeam, Long id);

    @Query("SELECT u.id FROM UserEntity u")
    List<Long> findAllUserIds();

    @Query("SELECT u.id FROM UserEntity u WHERE u.grade = :grade AND u.adminRole IS NULL")
    List<Long> findStudentIdsByGrade(@Param("grade") Integer grade);

    @Query("SELECT u.id FROM UserEntity u WHERE u.adminRole IS NULL")
    List<Long> findAllStudentIds();

    List<UserEntity> findAllByAdminRoleIsNotNull();

    @Query("SELECT u.id FROM UserEntity u WHERE u.adminRole IS NOT NULL")
    List<Long> findAllAdminUserIds();
}
