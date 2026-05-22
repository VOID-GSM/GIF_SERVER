package com.example.gifserverv2.domain.form.service;

import com.example.gifserverv2.domain.form.entity.Form;
import com.example.gifserverv2.domain.form.exception.FormException;
import com.example.gifserverv2.domain.form.repository.FormRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryFormService {

    private final FormRepository formRepository;

    public Form getFormOrThrow(Long formId) {
        return formRepository.findById(formId)
                .orElseThrow(FormException::notFound);
    }
}