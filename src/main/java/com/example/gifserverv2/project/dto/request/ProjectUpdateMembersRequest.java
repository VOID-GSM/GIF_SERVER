package com.example.gifserverv2.project.dto.request;

import java.util.List;

public record ProjectUpdateMembersRequest(
        List<Long> addMemberIds,
        List<Long> removeMemberIds
) {}
