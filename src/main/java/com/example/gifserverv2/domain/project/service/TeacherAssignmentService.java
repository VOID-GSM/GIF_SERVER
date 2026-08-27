package com.example.gifserverv2.domain.project.service;

import com.example.gifserverv2.domain.project.dto.request.RespondAssignmentRequest;
import com.example.gifserverv2.domain.project.dto.response.MyTeacherAssignmentResponse;
import com.example.gifserverv2.domain.project.entity.ProjectTeacherAssignment;
import com.example.gifserverv2.domain.project.entity.ProjectTeacherAssignment.AssignmentStatus;
import com.example.gifserverv2.domain.project.repository.ProjectTeacherAssignmentRepository;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherAssignmentService {

    private final ProjectTeacherAssignmentRepository assignmentRepository;

    @Transactional(readOnly = true)
    public List<MyTeacherAssignmentResponse> getMyAssignments(AuthenticatedUser currentUser) {
        List<ProjectTeacherAssignment> assignments =
                assignmentRepository.findAllByTeacherIdOrderByIdDesc(currentUser.userId());

        return assignments.stream()
                .map(a -> new MyTeacherAssignmentResponse(
                        a.getId(),
                        a.getProject().getId(),
                        a.getProject().getName(),
                        a.getProject().getTeamName(),
                        a.getStatus(),
                        a.getRejectReason()
                ))
                .toList();
    }

    @Transactional
    public void respondToAssignment(Long assignmentId, RespondAssignmentRequest request, AuthenticatedUser currentUser) {
        ProjectTeacherAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "배정 요청을 찾을 수 없습니다."));

        if (!assignment.getTeacher().getId().equals(currentUser.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 배정 요청만 처리할 수 있습니다.");
        }

        if (request.status() == AssignmentStatus.ACCEPTED) {
            assignment.accept();

            assignment.getProject().assignAdvisorTeacher(currentUser.userId());

            UserEntity teacher = assignment.getTeacher();
            teacher.updateAdminTeam(assignment.getProject().getTeamName());

        } else if (request.status() == AssignmentStatus.REJECTED) {
            if (request.rejectReason() == null || request.rejectReason().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "거절 사유를 작성해야 합니다.");
            }

            if (request.rejectReason().length() > 200) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "거절 사유는 200자를 초과할 수 없습니다.");
            }

            assignment.reject(request.rejectReason());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 응답 상태입니다.");
        }
    }
}