package com.example.gifserverv2.domain.project.service;

import com.example.gifserverv2.domain.project.dto.request.RespondAssignmentRequest;
import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.entity.ProjectTeacherAssignment;
import com.example.gifserverv2.domain.project.entity.ProjectTeacherAssignment.AssignmentStatus;
import com.example.gifserverv2.domain.project.repository.ProjectTeacherAssignmentRepository;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TeacherAssignmentService {

    private final ProjectTeacherAssignmentRepository assignmentRepository;

    @Transactional
    public void respondToAssignment(AuthenticatedUser user, Long assignmentId, RespondAssignmentRequest request) {
        ProjectTeacherAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 요청을 찾을 수 없습니다."));

        if (!assignment.getTeacher().getId().equals(user.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인에게 요청된 담당 교사 신청만 응답할 수 있습니다.");
        }

        if (assignment.getStatus() != AssignmentStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 처리되었거나 유효하지 않은 요청입니다.");
        }

        if (request.status() == AssignmentStatus.ACCEPTED) {
            assignment.accept();
            Project project = assignment.getProject();
            project.assignAdvisorTeacher(user.userId());
        } else if (request.status() == AssignmentStatus.REJECTED) {
            if (request.rejectReason() == null || request.rejectReason().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "거절 시에는 사유를 반드시 입력해야 합니다.");
            }
            assignment.reject(request.rejectReason());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 응답 상태입니다.");
        }
    }
}
