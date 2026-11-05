package com.example.gifserverv2.domain.student.dto.response;

import com.example.gifserverv2.domain.student.entity.TeamJoinRequest.Status;

import java.time.LocalDateTime;

public record TeamJoinRequestResponse(
        Long requestId,
        Long projectId,
        String projectName,
        String teamName,
        Long userId,
        String studentName,
        String studentNumber,
        Status status,
        String rejectReason,
        LocalDateTime createdAt
) {
}
