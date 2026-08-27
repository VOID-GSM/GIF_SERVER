package com.example.gifserverv2.domain.project.service;

import com.example.gifserverv2.domain.project.dto.request.AssignProjectTeacherRequest;
import com.example.gifserverv2.domain.project.dto.response.TeacherListResponse;
import com.example.gifserverv2.domain.project.dto.response.TeacherListResponse.AssignmentInfo;
import com.example.gifserverv2.domain.project.dto.response.TeacherListResponse.UnsubmittedProjectInfo;
import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.entity.ProjectTeacherAssignment;
import com.example.gifserverv2.domain.project.entity.ProjectTeacherAssignment.AssignmentStatus;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.domain.project.repository.ProjectTeacherAssignmentRepository;
import com.example.gifserverv2.domain.score.repository.ScoreRepository;
import com.example.gifserverv2.domain.user.entity.AdminRole;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminTeacherManagementService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ScoreRepository scoreRepository;
    private final ProjectTeacherAssignmentRepository assignmentRepository;

    @Transactional(readOnly = true)
    public List<TeacherListResponse> getAllTeachers(AuthenticatedUser user, Long projectId) {
        validateMasterRole(user);

        List<UserEntity> teachers = userRepository.findAllByAdminRoleIsNotNull();
        List<Project> allProjects = projectRepository.findAll();

        return teachers.stream().map(teacher -> {
            String evaluatorKey = teacher.getId().toString();

            Set<Long> evaluatedProjectIds = scoreRepository.findEvaluatedProjectIdsByEvaluatorId(evaluatorKey)
                    .stream().collect(Collectors.toSet());

            List<UnsubmittedProjectInfo> unsubmittedProjects = allProjects.stream()
                    .filter(project -> !evaluatedProjectIds.contains(project.getId()))
                    .map(project -> new UnsubmittedProjectInfo(
                            project.getId(),
                            project.getName(),
                            project.getTeamName()
                    ))
                    .toList();

            Optional<ProjectTeacherAssignment> assignmentOpt = (projectId != null)
                    ? assignmentRepository.findTopByProjectIdAndTeacherIdOrderByIdDesc(projectId, teacher.getId())
                    : assignmentRepository.findTopByTeacherIdOrderByIdDesc(teacher.getId());

            AssignmentInfo assignmentInfo = assignmentOpt
                    .map(a -> new AssignmentInfo(a.getProject().getId(), a.getStatus(), a.getRejectReason()))
                    .orElse(null);

            return new TeacherListResponse(
                    teacher.getId(),
                    teacher.getEmail(),
                    teacher.getName(),
                    teacher.getAdminRole(),
                    teacher.getAdminTeam(),
                    teacher.isGradeHead(),
                    assignmentInfo,
                    unsubmittedProjects.isEmpty(),
                    unsubmittedProjects
            );
        }).toList();
    }

    @Transactional
    public void assignTeacherToProject(AuthenticatedUser user, AssignProjectTeacherRequest request) {
        validateMasterRole(user);

        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."));

        UserEntity teacher = userRepository.findById(request.teacherId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 선생님을 찾을 수 없습니다."));

        if (teacher.getAdminRole() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지정 대상이 선생님 권한을 가지고 있지 않습니다.");
        }

        if (user.userId().equals(teacher.getId())) {
            ProjectTeacherAssignment assignment = ProjectTeacherAssignment.builder()
                    .project(project)
                    .teacher(teacher)
                    .status(AssignmentStatus.ACCEPTED)
                    .build();

            assignmentRepository.save(assignment);
            project.assignAdvisorTeacher(teacher.getId());
            teacher.updateAdminTeam(project.getTeamName());
            return;
        }

        boolean alreadyPending = assignmentRepository.existsByProjectIdAndTeacherIdAndStatus(
                project.getId(), teacher.getId(), AssignmentStatus.PENDING);

        if (alreadyPending) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 승인 대기 중인 담당 교사 요청이 있습니다.");
        }

        ProjectTeacherAssignment assignment = ProjectTeacherAssignment.builder()
                .project(project)
                .teacher(teacher)
                .status(AssignmentStatus.PENDING)
                .build();

        assignmentRepository.save(assignment);
    }

    private void validateMasterRole(AuthenticatedUser user) {
        if (user.adminRole() != AdminRole.MASTER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Master 교사만 접근할 수 있습니다.");
        }
    }
}