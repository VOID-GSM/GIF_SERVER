package com.example.gifserverv2.domain.student.repository;

import com.example.gifserverv2.domain.student.entity.StudentRoster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface StudentRosterRepository extends JpaRepository<StudentRoster, Long> {

    List<StudentRoster> findAllByStudentNumberIn(Collection<String> studentNumbers);

    List<StudentRoster> findAllByOrderByStudentNumberAsc();
}
