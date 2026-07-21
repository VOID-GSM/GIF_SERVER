package com.example.gifserverv2.domain.form.repository;

import com.example.gifserverv2.domain.form.entity.FormSubmit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FormSubmitRepository extends JpaRepository<FormSubmit, Long> {

    boolean existsByFormIdAndProjectId(Long formId, Long projectId);

    Optional<FormSubmit> findByFormIdAndProjectId(Long formId, Long projectId);

    List<FormSubmit> findAllByFormId(Long formId);

    List<FormSubmit> findAllByProjectId(Long projectId);

    @Query("select s from FormSubmit s join fetch s.answers a join fetch a.formField where s.id = :submitId")
    Optional<FormSubmit> findByIdWithAnswersAndFields(@Param("submitId") Long submitId);

    List<FormSubmit> findAllByFormIdIn(List<Long> formIds);
}