package com.example.gifserverv2.domain.project.dto.request;

import java.util.List;

public record CreateProjectRequest(
        String name,
        String teamName,
        String description,
        List<Long> memberIds
) {}