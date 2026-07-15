package com.example.gifserverv2.global.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class LocalFileStorageService implements FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.base-url}")
    private String baseUrl;

    @Override
    public String save(MultipartFile file, String directory) {
        validateFile(file);

        try {
            Path dirPath = Paths.get(uploadDir, directory);
            Files.createDirectories(dirPath);

            String originalFilename = file.getOriginalFilename();
            String extension = getExtension(originalFilename);
            String savedFilename = UUID.randomUUID() + "." + extension;

            Path filePath = dirPath.resolve(savedFilename);

            file.transferTo(filePath);

            return baseUrl + "/" + directory + "/" + savedFilename;

        } catch (IOException e) {
            throw new RuntimeException("파일 저장에 실패했습니다.", e);
        }
    }

    @Override
    public void delete(String fileUrl) {
        try {
            String relativePath = fileUrl.replace(baseUrl + "/", "");
            Path filePath = Paths.get(uploadDir, relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제에 실패했습니다.", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename).toLowerCase();
        if (!java.util.Set.of(
                "pdf", "ppt", "pptx", "doc", "docx", "hwp", "hwpx", "xls", "xlsx", "txt", "zip",
                "png", "jpg", "jpeg", "gif", "webp", "svg", "bmp",
                "mp4", "mov", "avi", "mkv",
                "mp3", "wav"
        ).contains(extension)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다.");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
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