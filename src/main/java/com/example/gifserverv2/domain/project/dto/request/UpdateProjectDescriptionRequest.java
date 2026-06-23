package com.example.gifserverv2.domain.project.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateProjectDescriptionRequest(
        @Size(max = 500, message = "프로젝트 설명은 500자 이하여야 합니다.")
        String description
) {}