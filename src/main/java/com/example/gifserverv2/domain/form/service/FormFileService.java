package com.example.gifserverv2.domain.form.service;

import com.example.gifserverv2.domain.form.exception.FormException;
import com.example.gifserverv2.domain.form.entity.FormFieldAnswer;
import com.example.gifserverv2.domain.form.entity.FormField;
import com.example.gifserverv2.domain.form.entity.FormSubmit;
import com.example.gifserverv2.domain.form.repository.FormFieldAnswerRepository;
import com.example.gifserverv2.domain.form.repository.FormFieldRepository;
import com.example.gifserverv2.domain.form.repository.FormSubmitRepository;
import com.example.gifserverv2.global.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FormFileService {

    private final FileStorageService fileStorageService;
    private final FormSubmitRepository formSubmitRepository;
    private final FormFieldRepository formFieldRepository;
    private final FormFieldAnswerRepository formFieldAnswerRepository;

    @Transactional
    public String uploadFile(Long submitId, Long fieldId, MultipartFile file) {
        FormSubmit submit = formSubmitRepository.findById(submitId)
                .orElseThrow(() -> new FormException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "제출 내역을 찾을 수 없습니다."));

        FormField field = formFieldRepository.findById(fieldId)
                .orElseThrow(FormException::fieldNotFound);

        if (field.getType() != FormField.FieldType.FILE) {
            throw new FormException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "파일 업로드 항목이 아닙니다.");
        }

        if (submit.getForm().isDeadlinePassed()) {
            throw FormException.deadlinePassed();
        }

        formFieldAnswerRepository.findByFormSubmitIdAndFormFieldId(submitId, fieldId)
                .ifPresent(existing -> {
                    if (existing.getFilePath() != null) {
                        fileStorageService.delete(existing.getFilePath());
                    }
                    formFieldAnswerRepository.delete(existing);
                });

        String savedUrl = fileStorageService.save(file, "form");

        formFieldAnswerRepository.save(FormFieldAnswer.builder()
                .formSubmit(submit)
                .formField(field)
                .filePath(savedUrl)
                .build());

        return savedUrl;
    }

    @Transactional
    public void deleteFile(Long submitId, Long fieldId) {
        FormFieldAnswer answer = formFieldAnswerRepository
                .findByFormSubmitIdAndFormFieldId(submitId, fieldId)
                .orElseThrow(() -> new FormException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."));

        if (answer.getFilePath() != null) {
            fileStorageService.delete(answer.getFilePath());
        }
        formFieldAnswerRepository.delete(answer);
    }
}
