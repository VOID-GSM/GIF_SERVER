package com.example.gifserverv2.domain.student.dto.response;

import com.example.gifserverv2.domain.user.entity.ClientRole;

import java.util.List;

public record StudentResponse(
        Long userId,
        String name,
        String studentNumber,
        String grade,
        String email,
        List<TeamInfo> teams,
        PendingRequestInfo pendingRequest
) {
    public record TeamInfo(Long projectId, String projectName, String teamName, ClientRole role) {
    }

    public record PendingRequestInfo(Long requestId, Long projectId, String projectName, String teamName) {
    }
}
