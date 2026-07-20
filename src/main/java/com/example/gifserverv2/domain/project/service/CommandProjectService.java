package com.example.gifserverv2.domain.project.service;

import com.example.gifserverv2.domain.project.dto.request.CreateProjectRequest;
import com.example.gifserverv2.domain.project.dto.request.TransferLeaderRequest;
import com.example.gifserverv2.domain.project.dto.request.UpdateProjectDescriptionRequest;
import com.example.gifserverv2.domain.project.dto.request.UpdateProjectRequest;
import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.entity.ProjectMember;
import com.example.gifserverv2.domain.project.exception.ProjectException;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.domain.push.entity.PushMessageTemplate;
import com.example.gifserverv2.domain.push.service.PushSenderService;
import com.example.gifserverv2.domain.user.entity.AdminRole;
import com.example.gifserverv2.domain.user.entity.ClientRole;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommandProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final QueryProjectService projectQueryService;
    private final ProjectLogoStorageService projectLogoStorageService;
    private final UserRepository userRepository;
    private final PushSenderService pushSenderService;

    @Transactional
    public void updateProject(Long projectId, Long userId, UpdateProjectRequest request, MultipartFile logo) {
        Project project = projectQueryService.getProjectOrThrow(projectId);
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
        }
        if (request.getGrade() != null) project.updateGrade(request.getGrade());

        if (summaryAffected) {
            project.clearAiSummary();
        }

        if (logo != null && !logo.isEmpty()) {
            replaceLogo(project, logo);
        }

        if (request.getAddMemberIds() != null || request.getRemoveMemberIds() != null) {
            List<ProjectMember> currentMembers = projectMemberRepository.findAllByProjectId(projectId);
            Map<Long, ProjectMember> memberMap = currentMembers.stream()
                    .collect(Collectors.toMap(ProjectMember::getUserId, member -> member));

            if (request.getAddMemberIds() != null) {
                request.getAddMemberIds().forEach(memberId -> {
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

                    pushSenderService.sendNotification(
                            memberId,
                            PushMessageTemplate.TEAM_MEMBER_ADDED.getTitle(),
                            PushMessageTemplate.TEAM_MEMBER_ADDED.formatBody(project.getName())
                    );
                });
            }

            if (request.getRemoveMemberIds() != null) {
                request.getRemoveMemberIds().forEach(memberId -> {
                    ProjectMember member = memberMap.get(memberId);
                    if (member == null) {
                        throw ProjectException.notMember();
                    }
                    if (member.getRole() == ClientRole.LEADER) {
                        throw ProjectException.cannotRemoveLeader();
                    }
                    projectMemberRepository.delete(member);
                    memberMap.remove(memberId);

                    pushSenderService.sendNotification(
                            memberId,
                            PushMessageTemplate.TEAM_MEMBER_REMOVED.getTitle(),
                            PushMessageTemplate.TEAM_MEMBER_REMOVED.formatBody(project.getName())
                    );
                });
            }
        }
    }

    @Transactional
    public Long createProject(Long userId, CreateProjectRequest request) {
        Project project = Project.builder()
                .name(request.name())
                .teamName(request.teamName())
                .description(request.description())
                .grade(request.grade())
                .build();

        Project savedProject = projectRepository.save(project);

        if (request.logo() != null && !request.logo().isEmpty()) {
            String logoUrl = projectLogoStorageService.save(request.logo());
            savedProject.updateLogo(logoUrl);
        }

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

            for (Long memberId : memberIds) {
                if (!userRepository.existsById(memberId)) {
                    throw new ProjectException(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다. userId: " + memberId);
                }

                ProjectMember member = ProjectMember.builder()
                        .project(savedProject)
                        .userId(memberId)
                        .role(ClientRole.MEMBER)
                        .build();
                projectMemberRepository.save(member);
            }
        }

        List<UserEntity> teachers = userRepository.findAll().stream()
                .filter(u -> u.getAdminRole() != null && u.getAdminRole().isAdmin())
                .toList();

        for (UserEntity teacher : teachers) {
            pushSenderService.sendNotification(
                    teacher.getId(),
                    PushMessageTemplate.PROJECT_CREATED.getTitle(),
                    PushMessageTemplate.PROJECT_CREATED.formatBody(savedProject.getName())
            );
        }

        return savedProject.getId();
    }

    @Transactional
    public void uploadLogo(Long projectId, Long userId, MultipartFile file) {
        Project project = projectQueryService.getProjectOrThrow(projectId);
        validateLeader(projectId, userId);

        replaceLogo(project, file);
    }

    private void replaceLogo(Project project, MultipartFile file) {
        String oldLogoUrl = project.getLogo();

        String newLogoUrl = projectLogoStorageService.save(file);
        project.updateLogo(newLogoUrl);

        if (oldLogoUrl != null && !oldLogoUrl.isBlank()) {
            projectLogoStorageService.delete(oldLogoUrl);
        }
    }

    private void validateLeader(Long projectId, Long userId) {
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(ProjectException::notMember);

        if (member.getRole() != ClientRole.LEADER) {
            throw ProjectException.notLeader();
        }
    }

    @Transactional
    public void updateDescription(Long projectId, Long userId, UpdateProjectDescriptionRequest request) {
        Project project = projectQueryService.getProjectOrThrow(projectId);

        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw ProjectException.notMember();
        }

        if (request != null && request.description() != null) {
            project.updateDescription(request.description());
            project.clearAiSummary();
        }
    }

    @Transactional
    public void transferLeader(Long projectId, Long userId, TransferLeaderRequest request) {
        UserEntity masterUser = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
        if (masterUser.getAdminRole() != AdminRole.MASTER) {
            throw new ProjectException(HttpStatus.FORBIDDEN, "팀장 양도 권한이 없습니다. (Master 선생님 전용)");
        }

        Project project = projectQueryService.getProjectOrThrow(projectId);

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
        for (Long memberUserId : memberUserIds) {
            pushSenderService.sendNotification(
                    memberUserId,
                    PushMessageTemplate.LEADER_TRANSFERRED_CLIENT.getTitle(),
                    PushMessageTemplate.LEADER_TRANSFERRED_CLIENT.formatBody(project.getName(), newLeaderName)
            );
        }

        List<UserEntity> teachers = userRepository.findAll().stream()
                .filter(u -> u.getAdminRole() != null && u.getAdminRole().isAdmin())
                .toList();

        for (UserEntity teacher : teachers) {
            pushSenderService.sendNotification(
                    teacher.getId(),
                    PushMessageTemplate.LEADER_TRANSFERRED_ADMIN.getTitle(),
                    PushMessageTemplate.LEADER_TRANSFERRED_ADMIN.formatBody(masterUser.getName(), project.getName(), newLeaderName)
            );
        }
    }
}