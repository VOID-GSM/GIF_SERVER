package com.example.gifserverv2.domain.form.repository;

import com.example.gifserverv2.domain.form.entity.Form;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormRepository extends JpaRepository<Form, Long> {

    List<Form> findAllByAnnouncedTrueOrderByDeadlineAsc();

    List<Form> findAllByOrderByCreatedAtDesc();

    List<Form> findAllByAnnouncedFalseOrderByCreatedAtDesc();
}
