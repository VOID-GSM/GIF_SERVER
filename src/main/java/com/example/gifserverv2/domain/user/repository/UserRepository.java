package com.example.gifserverv2.domain.user.repository;

import com.example.gifserverv2.domain.user.entity.AdminRole;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    /** 사용자별 개수 제한(예: 개인 링크 5개)을 동시 요청에서도 지키기 위해 사용자 행을 잠근다. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserEntity u WHERE u.id = :id")
    Optional<UserEntity> findByIdForUpdate(@Param("id") Long id);

    boolean existsByAdminTeamAndIdNotAndAdminRoleIsNotNull(String adminTeam, Long id);

    @Query("SELECT u.id FROM UserEntity u")
    List<Long> findAllUserIds();

    @Query("SELECT u.id FROM UserEntity u WHERE u.grade = :grade AND u.adminRole IS NULL")
    List<Long> findStudentIdsByGrade(@Param("grade") String grade);

    @Query("SELECT u.id FROM UserEntity u WHERE u.adminRole IS NULL")
    List<Long> findAllStudentIds();

    List<UserEntity> findAllByAdminRoleIsNotNull();

    @Query("SELECT u.id FROM UserEntity u WHERE u.adminRole = :adminRole")
    List<Long> findUserIdsByAdminRole(@Param("adminRole") AdminRole adminRole);

    Optional<UserEntity> findByGithubUsername(String githubUsername);
} 