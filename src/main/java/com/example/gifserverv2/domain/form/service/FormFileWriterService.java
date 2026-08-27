package com.example.gifserverv2.domain.form.service;

import com.example.gifserverv2.domain.form.entity.FormField;
import com.example.gifserverv2.domain.form.entity.FormFieldAnswer;
import com.example.gifserverv2.domain.form.entity.FormSubmit;
import com.example.gifserverv2.domain.form.exception.FormException;
import com.example.gifserverv2.domain.form.repository.FormFieldAnswerRepository;
import com.example.gifserverv2.domain.form.repository.FormFieldRepository;
import com.example.gifserverv2.domain.form.repository.FormSubmitRepository;
import com.example.gifserverv2.domain.project.exception.ProjectException;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FormFileWriterService {

    private final FormSubmitRepository formSubmitRepository;
    private final FormFieldRepository formFieldRepository;
    private final FormFieldAnswerRepository formFieldAnswerRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public record UploadPreparation(String existingFilePath) {}

    @Transactional(readOnly = true)
    public UploadPreparation prepareUpload(Long userId, Long submitId, Long fieldId, String extension) {
        FormSubmit submit = formSubmitRepository.findById(submitId)
                .orElseThrow(() -> new FormException(HttpStatus.NOT_FOUND, "제출 내역을 찾을 수 없습니다."));

        if (!projectMemberRepository.existsByProjectIdAndUserId(submit.getProjectId(), userId)) {
            throw ProjectException.notMember();
        }

        FormField field = formFieldRepository.findById(fieldId)
                .orElseThrow(FormException::fieldNotFound);

        if (field.getType() != FormField.FieldType.FILE) {
            throw new FormException(HttpStatus.BAD_REQUEST, "파일 업로드 항목이 아닙니다.");
        }

        if (!field.isExtensionAllowed(extension)) {
            throw FormException.disallowedFileExtension();
        }

        String existingFilePath = formFieldAnswerRepository.findByFormSubmitIdAndFormFieldId(submitId, fieldId)
                .map(FormFieldAnswer::getFilePath)
                .orElse(null);

        return new UploadPreparation(existingFilePath);
    }

    @Transactional
    public void persistUpload(Long submitId, Long fieldId, String savedUrl, Long fileSize, String originalFileName) {
        FormSubmit submit = formSubmitRepository.findById(submitId)
                .orElseThrow(() -> new FormException(HttpStatus.NOT_FOUND, "제출 내역을 찾을 수 없습니다."));
        FormField field = formFieldRepository.findById(fieldId)
                .orElseThrow(FormException::fieldNotFound);

        formFieldAnswerRepository.findByFormSubmitIdAndFormFieldId(submitId, fieldId)
                .ifPresent(formFieldAnswerRepository::delete);

        formFieldAnswerRepository.save(FormFieldAnswer.builder()
                .formSubmit(submit)
                .formField(field)
                .filePath(savedUrl)
                .fileSize(fileSize)
                .originalFileName(originalFileName)
                .build());

        submit.clearAiSummary();
    }

    @Transactional
    public String removeAnswer(Long userId, Long submitId, Long fieldId) {
        FormSubmit submit = formSubmitRepository.findById(submitId)
                .orElseThrow(() -> new FormException(HttpStatus.NOT_FOUND, "제출 내역을 찾을 수 없습니다."));

        if (!projectMemberRepository.existsByProjectIdAndUserId(submit.getProjectId(), userId)) {
            throw ProjectException.notMember();
        }

        FormFieldAnswer answer = formFieldAnswerRepository
                .findByFormSubmitIdAndFormFieldId(submitId, fieldId)
                .orElseThrow(() -> new FormException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."));

        String filePath = answer.getFilePath();
        formFieldAnswerRepository.delete(answer);
        submit.clearAiSummary();

        return filePath;
    }
}
