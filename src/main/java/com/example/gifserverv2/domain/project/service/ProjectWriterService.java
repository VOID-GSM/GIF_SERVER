package com.example.gifserverv2.domain.project.service;

import com.example.gifserverv2.domain.project.dto.request.CreateProjectRequest;
import com.example.gifserverv2.domain.project.dto.request.TransferLeaderRequest;
import com.example.gifserverv2.domain.project.dto.request.UpdateProjectRequest;
import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.entity.ProjectMember;
import com.example.gifserverv2.domain.project.event.ProjectTranslationRequestedEvent;
import com.example.gifserverv2.domain.project.exception.ProjectException;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.domain.student.repository.TeamJoinRequestRepository;
import com.example.gifserverv2.domain.user.entity.AdminRole;
import com.example.gifserverv2.domain.user.entity.ClientRole;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectWriterService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final TeamJoinRequestRepository teamJoinRequestRepository;
    private final ApplicationEventPublisher eventPublisher;

    public record CreatedProject(Long projectId, String projectName) {}

    public record ProjectMutationResult(String projectName) {}

    public record MemberReconciliation(List<Long> addedMemberIds, List<Long> removedMemberIds) {}

    public record LeaderTransferResult(
            List<Long> memberUserIds,
            List<Long> teacherIds,
            String projectName,
            String newLeaderName,
            String masterName
    ) {}

    @Transactional
    public CreatedProject createProject(Long userId, CreateProjectRequest request) {
        Project project = Project.builder()
                .name(request.name())
                .teamName(request.teamName())
                .description(request.description())
                .grade(request.grade())
                .build();

        Project savedProject = projectRepository.save(project);

        ProjectMember leader = ProjectMember.builder()
                .project(savedProject)
                .userId(userId)
                .role(ClientRole.LEADER)
                .build();
        projectMemberRepository.save(leader);

        if (request.memberIds() != null) {
            List<Long> memberIds = request.memberIds().stream()
                    .filter(id -> !id.equals(userId))
                    .toList();

            List<ProjectMember> existingMembers = projectMemberRepository.findAllByUserIdIn(memberIds);
            if (!existingMembers.isEmpty()) {
                throw new ProjectException(HttpStatus.CONFLICT, "이미 다른 프로젝트에 소속된 팀원이 포함되어 있습니다.");
            }

            if (!memberIds.isEmpty()) {
                List<UserEntity> foundUsers = userRepository.findAllById(memberIds);
                if (foundUsers.size() != memberIds.size()) {
                    Set<Long> foundIds = foundUsers.stream().map(UserEntity::getId).collect(Collectors.toSet());
                    Long missingId = memberIds.stream().filter(id -> !foundIds.contains(id)).findFirst().orElse(null);
                    throw new ProjectException(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다. userId: " + missingId);
                }

                List<ProjectMember> members = memberIds.stream()
                        .map(memberId -> ProjectMember.builder()
                                .project(savedProject)
                                .userId(memberId)
                                .role(ClientRole.MEMBER)
                                .build())
                        .toList();
                projectMemberRepository.saveAll(members);
            }
        }

        // 관리자 팀 배정 요청을 기다리던 학생이 이 경로로 팀에 들어가면 그 요청은 더는 수락할 수 없다. (#136)
        List<Long> joinedUserIds = new ArrayList<>();
        joinedUserIds.add(userId);
        if (request.memberIds() != null) joinedUserIds.addAll(request.memberIds());
        teamJoinRequestRepository.cancelPendingByUserIds(joinedUserIds);

        requestTranslationIfDescribed(savedProject);
        return new CreatedProject(savedProject.getId(), savedProject.getName());
    }

    @Transactional
    public void setLogo(Long projectId, String logoUrl) {
        Project project = projectRepository.findById(projectId).orElseThrow(ProjectException::notFound);
        project.updateLogo(logoUrl);
    }

    @Transactional(readOnly = true)
    public String getCurrentLogo(Long projectId) {
        return projectRepository.findById(projectId).orElseThrow(ProjectException::notFound).getLogo();
    }

    @Transactional(readOnly = true)
    public String prepareLogoUpload(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId).orElseThrow(ProjectException::notFound);
        validateLeader(projectId, userId);
        return project.getLogo();
    }

    @Transactional
    public ProjectMutationResult updateCoreFields(Long projectId, Long userId, UpdateProjectRequest request) {
        Project project = projectRepository.findById(projectId).orElseThrow(ProjectException::notFound);
        validateLeader(projectId, userId);

        boolean summaryAffected = false;

        if (request.getName() != null) {
            project.updateName(request.getName());
            summaryAffected = true;
        }
        if (request.getTeamName() != null) {
            project.updateTeamName(request.getTeamName());
            summaryAffected = true;
        }
        if (request.getDescription() != null) {
            project.updateDescription(request.getDescription());
            summaryAffected = true;
            requestTranslationIfDescribed(project);
        }
        if (request.getGrade() != null) {
            project.updateGrade(request.getGrade());
        }

        if (summaryAffected) {
            project.clearAiSummary();
        }

        return new ProjectMutationResult(project.getName());
    }

    // 커밋된 뒤 소개의 일본어 번역을 만든다. (ProjectTranslationService)
    private void requestTranslationIfDescribed(Project project) {
        if (project.getDescription() != null && !project.getDescription().isBlank()) {
            eventPublisher.publishEvent(new ProjectTranslationRequestedEvent(project.getId()));
        }
    }

    @Transactional
    public MemberReconciliation reconcileMembers(Long projectId, List<Long> addMemberIds, List<Long> removeMemberIds) {
        Project project = projectRepository.findById(projectId).orElseThrow(ProjectException::notFound);

        List<ProjectMember> currentMembers = projectMemberRepository.findAllByProjectId(projectId);
        Map<Long, ProjectMember> memberMap = currentMembers.stream()
                .collect(Collectors.toMap(ProjectMember::getUserId, member -> member));

        List<Long> addedIds = new ArrayList<>();
        if (addMemberIds != null) {
            for (Long memberId : addMemberIds) {
                if (memberMap.containsKey(memberId)) {
                    throw ProjectException.alreadyMember();
                }
                ProjectMember newMember = ProjectMember.builder()
                        .project(project)
                        .userId(memberId)
                        .role(ClientRole.MEMBER)
                        .build();
                projectMemberRepository.save(newMember);
                memberMap.put(memberId, newMember);
                addedIds.add(memberId);
            }
        }

        List<Long> removedIds = new ArrayList<>();
        if (removeMemberIds != null) {
            for (Long memberId : removeMemberIds) {
                ProjectMember member = memberMap.get(memberId);
                if (member == null) {
                    throw ProjectException.notMember();
                }
                if (member.getRole() == ClientRole.LEADER) {
                    throw ProjectException.cannotRemoveLeader();
                }
                projectMemberRepository.delete(member);
                memberMap.remove(memberId);
                removedIds.add(memberId);
            }
        }

        // 관리자 팀 배정 요청을 기다리던 학생이 이 경로로 팀에 들어가면 그 요청은 더는 수락할 수 없다. (#136)
        teamJoinRequestRepository.cancelPendingByUserIds(addedIds);

        return new MemberReconciliation(addedIds, removedIds);
    }

    @Transactional
    public LeaderTransferResult transferLeader(Long projectId, Long userId, TransferLeaderRequest request) {
        UserEntity masterUser = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
        if (masterUser.getAdminRole() != AdminRole.MASTER) {
            throw new ProjectException(HttpStatus.FORBIDDEN, "팀장 양도 권한이 없습니다. (Master 선생님 전용)");
        }

        Project project = projectRepository.findById(projectId).orElseThrow(ProjectException::notFound);

        ProjectMember currentLeader = projectMemberRepository.findByProjectIdAndRole(projectId, ClientRole.LEADER)
                .orElseThrow(() -> new ProjectException(HttpStatus.NOT_FOUND, "해당 프로젝트에 팀장이 존재하지 않습니다."));

        ProjectMember newLeader = projectMemberRepository.findByProjectIdAndUserId(projectId, request.newLeaderUserId())
                .orElseThrow(() -> new ProjectException(HttpStatus.NOT_FOUND, "새로운 팀장 대상자가 프로젝트의 멤버가 아닙니다."));

        if (currentLeader.getUserId().equals(newLeader.getUserId())) {
            throw new ProjectException(HttpStatus.BAD_REQUEST, "대상자는 이미 해당 프로젝트의 팀장입니다.");
        }

        boolean isAlreadyLeaderElsewhere = projectMemberRepository.existsByUserIdAndRole(newLeader.getUserId(), ClientRole.LEADER);
        if (isAlreadyLeaderElsewhere) {
            throw new ProjectException(HttpStatus.CONFLICT, "대상자는 이미 다른 프로젝트의 팀장으로 지정되어 있습니다.");
        }

        currentLeader.changeRole(ClientRole.MEMBER);
        newLeader.changeRole(ClientRole.LEADER);

        String newLeaderName = userRepository.findById(request.newLeaderUserId())
                .map(UserEntity::getName)
                .orElse("유저");

        List<Long> memberUserIds = projectMemberRepository.findUserIdsByProjectId(projectId);
        List<Long> teacherIds = userRepository.findAllByAdminRoleIsNotNull().stream()
                .map(UserEntity::getId)
                .toList();

        return new LeaderTransferResult(memberUserIds, teacherIds, project.getName(), newLeaderName, masterUser.getName());
    }

    private void validateLeader(Long projectId, Long userId) {
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(ProjectException::notMember);

        if (member.getRole() != ClientRole.LEADER) {
            throw ProjectException.notLeader();
        }
    }
}
