package com.example.gifserverv2.domain.form.service;

import com.example.gifserverv2.domain.form.dto.response.FileUploadResponse;
import com.example.gifserverv2.domain.form.exception.FormException;
import com.example.gifserverv2.domain.form.entity.FormField;
import com.example.gifserverv2.domain.form.entity.FormFieldAnswer;
import com.example.gifserverv2.domain.form.entity.FormSubmit;
import com.example.gifserverv2.domain.form.repository.FormFieldAnswerRepository;
import com.example.gifserverv2.domain.form.repository.FormFieldRepository;
import com.example.gifserverv2.domain.form.repository.FormSubmitRepository;
import com.example.gifserverv2.domain.project.exception.ProjectException;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import com.example.gifserverv2.global.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final ProjectMemberRepository projectMemberRepository;

    @Transactional
    public FileUploadResponse uploadFile(Long userId, Long submitId, Long fieldId, MultipartFile file) {
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

        if (submit.getForm().isDeadlinePassed()) {
            throw FormException.deadlinePassed();
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName != null) {
            originalFileName = org.springframework.util.StringUtils.getFilename(
                    org.springframework.util.StringUtils.cleanPath(originalFileName)
            );
        }

        String extension = extractExtension(originalFileName);
        if (!field.isExtensionAllowed(extension)) {
            throw FormException.disallowedFileExtension();
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
                .fileSize(file.getSize())
                .originalFileName(originalFileName)
                .build());

        return new FileUploadResponse(savedUrl, originalFileName);
    }

    @Transactional
    public void deleteFile(Long userId, Long submitId, Long fieldId) {
        FormSubmit submit = formSubmitRepository.findById(submitId)
                .orElseThrow(() -> new FormException(HttpStatus.NOT_FOUND, "제출 내역을 찾을 수 없습니다."));

        if (!projectMemberRepository.existsByProjectIdAndUserId(submit.getProjectId(), userId)) {
            throw ProjectException.notMember();
        }

        if (submit.getForm().isDeadlinePassed()) {
            throw FormException.deadlinePassed();
        }

        FormFieldAnswer answer = formFieldAnswerRepository
                .findByFormSubmitIdAndFormFieldId(submitId, fieldId)
                .orElseThrow(() -> new FormException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."));

        if (answer.getFilePath() != null) {
            fileStorageService.delete(answer.getFilePath());
        }
        formFieldAnswerRepository.delete(answer);
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new FormException(HttpStatus.BAD_REQUEST, "파일 확장자가 없습니다.");
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}