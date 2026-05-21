package com.example.gifserverv2.domain.form.repository;

import com.example.gifserverv2.domain.form.entity.FormSubmit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FormSubmitRepository extends JpaRepository<FormSubmit, Long> {
    
    boolean existsByFormId(Long formId);

    boolean existsByFormIdAndProjectId(Long formId, Long projectId);

    Optional<FormSubmit> findByFormIdAndProjectId(Long formId, Long projectId);

    List<FormSubmit> findAllByFormId(Long formId);

    List<FormSubmit> findAllByProjectId(Long projectId);
}