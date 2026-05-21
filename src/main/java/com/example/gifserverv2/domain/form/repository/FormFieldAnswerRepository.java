package com.example.gifserverv2.domain.form.repository;

import com.example.gifserverv2.domain.form.entity.FormFieldAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormFieldAnswerRepository extends JpaRepository<FormFieldAnswer, Long> {

    List<FormFieldAnswer> findAllByFormSubmitId(Long formSubmitId);

    void deleteAllByFormSubmitId(Long formSubmitId);
}