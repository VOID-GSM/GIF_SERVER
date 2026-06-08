package com.example.gifserverv2.domain.project.dto.request;

import java.util.List;

public record UpdateProjectRequest(
        String name,
        String teamName,
        String description,
        List<Long> addMemberIds,
        List<Long> removeMemberIds
) {}