package com.example.gifserverv2.domain.project.repository;

import com.example.gifserverv2.domain.user.entity.ProjectTeacherAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectTeacherAssignmentRepository extends JpaRepository<ProjectTeacherAssignment, Long> {

    Optional<ProjectTeacherAssignment> findByProjectIdAndTeacherId(Long projectId, Long teacherId);

    boolean existsByProjectIdAndTeacherIdAndStatus(Long projectId, Long teacherId, ProjectTeacherAssignment.AssignmentStatus status);
}
