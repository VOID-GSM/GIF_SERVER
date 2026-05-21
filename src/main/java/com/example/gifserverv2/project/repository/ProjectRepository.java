package com.example.gifserverv2.project.repository;

import com.example.gifserverv2.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("""
    SELECT DISTINCT p FROM Project p
    JOIN p.members pm
    JOIN UserEntity u ON u.id = pm.userId
    WHERE SUBSTRING(u.studentNumber, 1, 1) = :grade
    """)
    List<Project> findAllByGrade(@Param("grade") String grade);

}