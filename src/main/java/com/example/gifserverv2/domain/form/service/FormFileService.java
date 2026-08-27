package com.example.gifserverv2.domain.form.service;

import com.example.gifserverv2.domain.form.dto.response.FileUploadResponse;
import com.example.gifserverv2.domain.form.exception.FormException;
import com.example.gifserverv2.global.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FormFileService {

    private final FileStorageService fileStorageService;
    private final FormFileWriterService formFileWriter;

    public FileUploadResponse uploadFile(Long userId, Long submitId, Long fieldId, MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName != null) {
            originalFileName = org.springframework.util.StringUtils.getFilename(
                    org.springframework.util.StringUtils.cleanPath(originalFileName)
            );
        }
        String extension = extractExtension(originalFileName);

        FormFileWriterService.UploadPreparation preparation =
                formFileWriter.prepareUpload(userId, submitId, fieldId, extension);

        String savedUrl = fileStorageService.save(file, "form");

        try {
            formFileWriter.persistUpload(submitId, fieldId, savedUrl, file.getSize(), originalFileName);
        } catch (RuntimeException e) {
            fileStorageService.delete(savedUrl);
            throw e;
        }

        if (preparation.existingFilePath() != null) {
            fileStorageService.delete(preparation.existingFilePath());
        }

        return new FileUploadResponse(savedUrl, originalFileName);
    }

    public void deleteFile(Long userId, Long submitId, Long fieldId) {
        String filePathToDelete = formFileWriter.removeAnswer(userId, submitId, fieldId);
        if (filePathToDelete != null) {
            fileStorageService.delete(filePathToDelete);
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new FormException(HttpStatus.BAD_REQUEST, "파일 확장자가 없습니다.");
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
