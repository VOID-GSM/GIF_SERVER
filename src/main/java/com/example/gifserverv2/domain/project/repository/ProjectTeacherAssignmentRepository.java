package com.example.gifserverv2.domain.project.repository;

import com.example.gifserverv2.domain.project.entity.ProjectTeacherAssignment;
import com.example.gifserverv2.domain.project.entity.ProjectTeacherAssignment.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectTeacherAssignmentRepository extends JpaRepository<ProjectTeacherAssignment, Long> {

    boolean existsByProjectIdAndTeacherIdAndStatus(Long projectId, Long teacherId, AssignmentStatus status);

    Optional<ProjectTeacherAssignment> findTopByTeacherIdOrderByIdDesc(Long teacherId);

    Optional<ProjectTeacherAssignment> findTopByProjectIdAndTeacherIdOrderByIdDesc(Long projectId, Long teacherId);

    List<ProjectTeacherAssignment> findAllByTeacherIdOrderByIdDesc(Long teacherId);
}