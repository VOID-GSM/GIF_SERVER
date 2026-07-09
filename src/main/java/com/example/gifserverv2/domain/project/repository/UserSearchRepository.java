package com.example.gifserverv2.domain.project.repository;

import com.example.gifserverv2.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSearchRepository extends JpaRepository<UserEntity, Long> {

    List<UserEntity> findByNameContainingOrStudentNumberContaining(String name, String studentNumber);
}