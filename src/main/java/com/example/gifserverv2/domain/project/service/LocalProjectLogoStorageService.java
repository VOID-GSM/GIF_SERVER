package com.example.gifserverv2.domain.project.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Component
public class LocalProjectLogoStorageService implements ProjectLogoStorageService {

    private static final String PROJECT_LOGO_DIR = "project/logos";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp");

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.base-url}")
    private String baseUrl;

    @Override
    public String save(MultipartFile file) {
        validateFile(file);

        try {
            Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path dirPath = basePath.resolve(PROJECT_LOGO_DIR).normalize();

            Files.createDirectories(dirPath);

            String extension = getExtension(file.getOriginalFilename()).toLowerCase();
            String savedFilename = UUID.randomUUID() + "." + extension;
            Path filePath = dirPath.resolve(savedFilename).normalize();

            file.transferTo(filePath.toFile());

            return baseUrl + "/" + PROJECT_LOGO_DIR + "/" + savedFilename;
        } catch (IOException e) {
            throw new RuntimeException("프로젝트 로고 저장에 실패했습니다.", e);
        }
    }

    @Override
    public void delete(String logoUrl) {
        if (logoUrl == null || logoUrl.isBlank()) {
            return;
        }

        try {
            String relativePath = logoUrl.replace(baseUrl + "/", "");
            Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = basePath.resolve(relativePath).normalize();

            if (!filePath.startsWith(basePath)) {
                throw new IllegalArgumentException("잘못된 파일 경로입니다.");
            }

            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("프로젝트 로고 삭제에 실패했습니다.", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        String extension = getExtension(file.getOriginalFilename()).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("프로젝트 로고는 png, jpg, jpeg, webp 형식만 업로드할 수 있습니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("파일 확장자가 없습니다.");
        }

        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
