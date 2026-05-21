package com.example.gifserverv2.project.dto.request;

import java.util.List;

public record ProjectCreateRequest(
        String name,
        String teamName,
        String description,
        List<Long> memberIds
) {}