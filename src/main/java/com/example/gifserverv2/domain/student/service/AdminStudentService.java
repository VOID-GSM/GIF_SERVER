package com.example.gifserverv2.domain.student.service;

import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.entity.ProjectMember;
import com.example.gifserverv2.domain.project.exception.ProjectException;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.domain.push.entity.PushMessageTemplate;
import com.example.gifserverv2.domain.student.dto.response.StudentResponse;
import com.example.gifserverv2.domain.student.dto.response.StudentResponse.PendingRequestInfo;
import com.example.gifserverv2.domain.student.dto.response.StudentResponse.TeamInfo;
import com.example.gifserverv2.domain.student.dto.response.TeamJoinRequestResponse;
import com.example.gifserverv2.domain.student.entity.TeamJoinRequest;
import com.example.gifserverv2.domain.student.entity.TeamJoinRequest.Status;
import com.example.gifserverv2.domain.student.exception.StudentException;
import com.example.gifserverv2.domain.student.repository.TeamJoinRequestRepository;
import com.example.gifserverv2.domain.user.entity.ClientRole;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminStudentService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TeamJoinRequestRepository teamJoinRequestRepository;
    private final StudentNotifier notifier;

    /**
     * 학생 목록. keyword 는 이름·학번 부분 일치, assigned 가 false 면 어느 팀에도 속하지 않은 학생만 준다.
     */
    @Transactional(readOnly = true)
    public List<StudentResponse> getStudents(String keyword, String grade, Boolean assigned) {
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.strip();

        List<UserEntity> students = userRepository.findAllStudents().stream()
                .filter(u -> grade == null || grade.equals(u.getGrade()))
                .filter(u -> normalizedKeyword == null
                        || contains(u.getName(), normalizedKeyword)
                        || contains(u.getStudentNumber(), normalizedKeyword))
                .toList();
        if (students.isEmpty()) return List.of();

        List<Long> userIds = students.stream().map(UserEntity::getId).toList();
        Map<Long, List<ProjectMember>> membershipsByUser = projectMemberRepository.findAllByUserIdIn(userIds).stream()
                .collect(Collectors.groupingBy(ProjectMember::getUserId));
        Map<Long, TeamJoinRequest> pendingByUser = teamJoinRequestRepository
                .findAllByUserIdInAndStatus(userIds, Status.PENDING).stream()
                .collect(Collectors.toMap(TeamJoinRequest::getUserId, Function.identity(), (a, b) -> a));

        return students.stream()
                .filter(u -> assigned == null || assigned == membershipsByUser.containsKey(u.getId()))
                .map(u -> toResponse(u, membershipsByUser.getOrDefault(u.getId(), List.of()), pendingByUser.get(u.getId())))
                .toList();
    }

    /** 미배정 학생을 프로젝트에 추가해 달라고 팀장에게 요청한다. 팀장이 수락해야 팀원이 된다. */
    @Transactional
    public TeamJoinRequestResponse requestAssign(AuthenticatedUser caller, Long userId, Long projectId) {
        StudentManagerGuard.requireManager(caller);

        UserEntity student = lockStudent(userId);
        Project project = projectRepository.findById(projectId).orElseThrow(ProjectException::notFound);

        if (projectMemberRepository.existsByUserId(userId)) {
            throw StudentException.alreadyAssigned();
        }
        if (teamJoinRequestRepository.existsByUserIdAndStatus(userId, Status.PENDING)) {
            throw new StudentException(HttpStatus.CONFLICT, "이미 승인 대기 중인 팀원 추가 요청이 있습니다.");
        }
        ProjectMember leader = projectMemberRepository.findByProjectIdAndRole(projectId, ClientRole.LEADER)
                .orElseThrow(() -> new StudentException(HttpStatus.BAD_REQUEST, "팀장이 없는 프로젝트에는 요청할 수 없습니다."));

        TeamJoinRequest request = teamJoinRequestRepository.save(new TeamJoinRequest(project, userId, caller.userId()));

        notifier.send(leader.getUserId(), PushMessageTemplate.TEAM_JOIN_REQUESTED,
                PushMessageTemplate.TEAM_JOIN_REQUESTED.formatBody(student.getName(), project.getName()),
                PushMessageTemplate.TEAM_JOIN_REQUESTED.formatTargetUrl(projectId),
                PushMessageTemplate.TEAM_JOIN_REQUESTED.formatTag(request.getId()));

        return TeamJoinRequestMapper.toResponse(request, student);
    }

    @Transactional(readOnly = true)
    public List<TeamJoinRequestResponse> getPendingRequests(AuthenticatedUser caller) {
        StudentManagerGuard.requireManager(caller);
        return TeamJoinRequestMapper.toResponses(
                teamJoinRequestRepository.findAllByStatusOrderByIdDesc(Status.PENDING), userRepository);
    }

    @Transactional
    public void cancelRequest(AuthenticatedUser caller, Long requestId) {
        StudentManagerGuard.requireManager(caller);
        TeamJoinRequest request = teamJoinRequestRepository.findById(requestId)
                .orElseThrow(StudentException::requestNotFound);
        if (!request.isPending()) {
            throw StudentException.requestClosed();
        }
        request.cancel();
    }

    /** 이미 팀에 속한 학생을 다른 팀으로 옮긴다. 관리자 권한으로 바로 반영한다. */
    @Transactional
    public void moveTeam(AuthenticatedUser caller, Long userId, Long projectId) {
        StudentManagerGuard.requireManager(caller);

        lockStudent(userId);
        Project target = projectRepository.findById(projectId).orElseThrow(ProjectException::notFound);

        List<ProjectMember> memberships = projectMemberRepository.findAllByUserId(userId);
        if (memberships.isEmpty()) {
            throw StudentException.notAssigned();
        }
        if (memberships.stream().anyMatch(m -> m.getProject().getId().equals(projectId))) {
            throw new StudentException(HttpStatus.BAD_REQUEST, "이미 해당 팀에 소속된 학생입니다.");
        }
        if (memberships.stream().anyMatch(m -> m.getRole() == ClientRole.LEADER)) {
            throw StudentException.leaderCannotLeave();
        }

        projectMemberRepository.deleteAll(memberships);
        projectMemberRepository.save(ProjectMember.builder()
                .project(target)
                .userId(userId)
                .role(ClientRole.MEMBER)
                .build());

        for (ProjectMember previous : memberships) {
            notifier.send(userId, PushMessageTemplate.TEAM_MEMBER_REMOVED,
                    PushMessageTemplate.TEAM_MEMBER_REMOVED.formatBody(previous.getProject().getName()),
                    PushMessageTemplate.TEAM_MEMBER_REMOVED.formatTargetUrl(),
                    PushMessageTemplate.TEAM_MEMBER_REMOVED.formatTag(previous.getProject().getId()));
        }
        notifier.send(userId, PushMessageTemplate.TEAM_MEMBER_ADDED,
                PushMessageTemplate.TEAM_MEMBER_ADDED.formatBody(target.getName()),
                PushMessageTemplate.TEAM_MEMBER_ADDED.formatTargetUrl(projectId),
                PushMessageTemplate.TEAM_MEMBER_ADDED.formatTag(projectId));
    }

    @Transactional
    public void removeFromTeam(AuthenticatedUser caller, Long userId, Long projectId) {
        StudentManagerGuard.requireManager(caller);

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(ProjectException::notMember);
        if (member.getRole() == ClientRole.LEADER) {
            throw StudentException.leaderCannotLeave();
        }
        String projectName = member.getProject().getName();
        projectMemberRepository.delete(member);

        notifier.send(userId, PushMessageTemplate.TEAM_MEMBER_REMOVED,
                PushMessageTemplate.TEAM_MEMBER_REMOVED.formatBody(projectName),
                PushMessageTemplate.TEAM_MEMBER_REMOVED.formatTargetUrl(),
                PushMessageTemplate.TEAM_MEMBER_REMOVED.formatTag(projectId));
    }

    /**
     * 팀 안에서 역할을 바꾼다. 팀장으로 지정하면 기존 팀장은 팀원이 된다.
     * 팀에는 팀장이 항상 한 명 있어야 하므로, 현재 팀장을 팀원으로 바꾸려면 다른 팀원을 팀장으로 지정해야 한다.
     */
    @Transactional
    public void changeRole(AuthenticatedUser caller, Long userId, Long projectId, ClientRole role) {
        StudentManagerGuard.requireManager(caller);

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(ProjectException::notMember);
        if (member.getRole() == role) {
            return;
        }
        if (role == ClientRole.MEMBER) {
            throw new StudentException(HttpStatus.BAD_REQUEST, "팀장을 팀원으로 바꾸려면 다른 팀원을 팀장으로 지정해 주세요.");
        }
        if (projectMemberRepository.existsByUserIdAndRole(userId, ClientRole.LEADER)) {
            throw new StudentException(HttpStatus.CONFLICT, "이미 다른 프로젝트의 팀장으로 지정된 학생입니다.");
        }

        projectMemberRepository.findByProjectIdAndRole(projectId, ClientRole.LEADER)
                .ifPresent(currentLeader -> currentLeader.changeRole(ClientRole.MEMBER));
        member.changeRole(ClientRole.LEADER);

        String projectName = member.getProject().getName();
        String newLeaderName = userRepository.findById(userId).map(UserEntity::getName).orElse("학생");
        notifier.sendAll(projectMemberRepository.findUserIdsByProjectId(projectId),
                PushMessageTemplate.LEADER_TRANSFERRED_CLIENT,
                PushMessageTemplate.LEADER_TRANSFERRED_CLIENT.formatBody(projectName, newLeaderName),
                PushMessageTemplate.LEADER_TRANSFERRED_CLIENT.formatTargetUrl(projectId),
                PushMessageTemplate.LEADER_TRANSFERRED_CLIENT.formatTag(projectId));
    }

    /** 같은 학생을 동시에 두 팀에 배정하지 않도록 학생 행을 잠근다. */
    private UserEntity lockStudent(Long userId) {
        UserEntity user = userRepository.findByIdForUpdate(userId).orElseThrow(StudentException::studentNotFound);
        if (user.getAdminRole() != null || user.getRole() != Role.USER) {
            throw StudentException.studentNotFound();
        }
        return user;
    }

    private static boolean contains(String value, String keyword) {
        return value != null && value.contains(keyword);
    }

    private static StudentResponse toResponse(UserEntity user, List<ProjectMember> memberships, TeamJoinRequest pending) {
        List<TeamInfo> teams = memberships.stream()
                .map(m -> new TeamInfo(m.getProject().getId(), m.getProject().getName(), m.getProject().getTeamName(), m.getRole()))
                .toList();
        PendingRequestInfo pendingInfo = pending == null ? null : new PendingRequestInfo(
                pending.getId(), pending.getProject().getId(), pending.getProject().getName(), pending.getProject().getTeamName());
        return new StudentResponse(user.getId(), user.getName(), user.getStudentNumber(), user.getGrade(),
                user.getEmail(), teams, pendingInfo);
    }
}
