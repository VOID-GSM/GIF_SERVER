package com.example.gifserverv2.domain.form.dto.response;

public record FileUploadResponse(
        String filePath,
        String originalFileName
) {}

