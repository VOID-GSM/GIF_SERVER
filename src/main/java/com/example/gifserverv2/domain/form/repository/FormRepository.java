package com.example.gifserverv2.domain.form.repository;

import com.example.gifserverv2.domain.form.entity.Form;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FormRepository extends JpaRepository<Form, Long> {

    List<Form> findAllByAnnouncedTrueOrderByDeadlineAsc();
    List<Form> findAllByAnnouncedFalseOrderByCreatedAtDesc();
    List<Form> findAllByTargetGrade(Integer targetGrade);
    Optional<Form> findByIdAndAnnouncedFalse(Long id);
    List<Form> findAllByAnnouncedTrueAndDeadlineAfter(LocalDateTime now);
}
