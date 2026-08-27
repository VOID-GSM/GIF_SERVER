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

/**
 * FormFileService의 DB 조회/저장만 담당하는 트랜잭션 경계.
 * 디스크 파일 저장/삭제(FileStorageService)가 DB 트랜잭션 안에서 일어나
 * 커넥션을 오래 점유하는 걸 막기 위해 FormFileService에서 분리했다.
 * (ClientInquiryService / InquiryWriterService와 동일한 분리 패턴)
 */
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

    /**
     * 답변 파일을 DB에서 삭제하고, 디스크에서 지워야 할 파일 경로를 반환한다.
     * (실제 디스크 삭제는 호출자가 트랜잭션 밖에서 수행)
     */
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
