package com.example.gifserverv2.domain.form.repository;

import com.example.gifserverv2.domain.form.entity.FormField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormFieldRepository extends JpaRepository<FormField, Long> {

    List<FormField> findAllByFormIdOrderByOrderIndexAsc(Long formId);

    void deleteAllByFormId(Long formId);
}
