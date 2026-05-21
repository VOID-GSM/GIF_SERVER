package com.example.gifserverv2.domain.project.dto.request;

import java.util.List;

public record UpdateMembersProjectRequest(
        List<Long> addMemberIds,
        List<Long> removeMemberIds
) {}
