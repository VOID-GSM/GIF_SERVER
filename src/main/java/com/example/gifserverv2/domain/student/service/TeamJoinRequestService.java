package com.example.gifserverv2.domain.student.service;

import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.entity.ProjectMember;
import com.example.gifserverv2.domain.project.exception.ProjectException;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import com.example.gifserverv2.domain.push.entity.PushMessageTemplate;
import com.example.gifserverv2.domain.student.dto.response.TeamJoinRequestResponse;
import com.example.gifserverv2.domain.student.entity.TeamJoinRequest;
import com.example.gifserverv2.domain.student.entity.TeamJoinRequest.Status;
import com.example.gifserverv2.domain.student.exception.StudentException;
import com.example.gifserverv2.domain.student.repository.TeamJoinRequestRepository;
import com.example.gifserverv2.domain.user.entity.ClientRole;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 관리자가 보낸 팀원 추가 요청을 팀장이 수락/거절한다. (#136) */
@Service
@RequiredArgsConstructor
public class TeamJoinRequestService {

    private final TeamJoinRequestRepository teamJoinRequestRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final StudentNotifier notifier;

    @Transactional(readOnly = true)
    public List<TeamJoinRequestResponse> getPendingRequests(AuthenticatedUser caller, Long projectId) {
        if (!StudentManagerGuard.isManager(caller)) {
            requireLeader(projectId, caller.userId());
        }
        return TeamJoinRequestMapper.toResponses(
                teamJoinRequestRepository.findAllByProject_IdAndStatusOrderByIdDesc(projectId, Status.PENDING),
                userRepository);
    }

    /**
     * 수락 시점에 학생이 이미 다른 경로로 팀에 들어가 있으면 요청을 취소로 닫고 409 를 준다.
     * 닫은 상태가 롤백되지 않도록 StudentException 은 롤백하지 않는다.
     * (그 외 StudentException 은 모두 데이터를 바꾸기 전에 던진다.)
     */
    @Transactional(noRollbackFor = StudentException.class)
    public void respond(AuthenticatedUser caller, Long requestId, boolean accept, String rejectReason) {
        TeamJoinRequest request = teamJoinRequestRepository.findById(requestId)
                .orElseThrow(StudentException::requestNotFound);
        Project project = request.getProject();
        requireLeader(project.getId(), caller.userId());

        // 같은 학생에 대한 배정·이동과 겹치지 않도록 학생 행을 잠근 뒤 상태를 다시 확인한다.
        UserEntity student = userRepository.findByIdForUpdate(request.getUserId())
                .orElseThrow(StudentException::studentNotFound);
        if (!request.isPending()) {
            throw StudentException.requestClosed();
        }

        if (!accept) {
            String reason = (rejectReason == null || rejectReason.isBlank()) ? null : rejectReason.strip();
            request.reject(reason);
            notifier.send(request.getRequestedBy(), PushMessageTemplate.TEAM_JOIN_REJECTED,
                    PushMessageTemplate.TEAM_JOIN_REJECTED.formatBody(project.getName(), student.getName()),
                    PushMessageTemplate.TEAM_JOIN_REJECTED.formatTargetUrl(project.getId()),
                    PushMessageTemplate.TEAM_JOIN_REJECTED.formatTag(request.getId()));
            return;
        }

        if (projectMemberRepository.existsByUserId(student.getId())) {
            request.cancel();
            throw new StudentException(HttpStatus.CONFLICT, "학생이 이미 다른 팀에 소속되어 요청이 취소되었습니다.");
        }
        request.accept();
        projectMemberRepository.save(ProjectMember.builder()
                .project(project)
                .userId(student.getId())
                .role(ClientRole.MEMBER)
                .build());

        notifier.send(student.getId(), PushMessageTemplate.TEAM_MEMBER_ADDED,
                PushMessageTemplate.TEAM_MEMBER_ADDED.formatBody(project.getName()),
                PushMessageTemplate.TEAM_MEMBER_ADDED.formatTargetUrl(project.getId()),
                PushMessageTemplate.TEAM_MEMBER_ADDED.formatTag(project.getId()));
    }

    private void requireLeader(Long projectId, Long userId) {
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(ProjectException::notMember);
        if (member.getRole() != ClientRole.LEADER) {
            throw ProjectException.notLeader();
        }
    }
}
