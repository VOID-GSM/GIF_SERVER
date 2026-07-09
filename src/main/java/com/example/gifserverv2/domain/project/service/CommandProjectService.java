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
import com.example.gifserverv2.domain.user.entity.ClientRole;
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
@Transactional
public class CommandProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final QueryProjectService projectQueryService;
    private final ProjectLogoStorageService projectLogoStorageService;
    private final UserRepository userRepository;

    public void updateProject(Long projectId, Long userId, UpdateProjectRequest request, MultipartFile logo) {
        Project project = projectQueryService.getProjectOrThrow(projectId);
        validateLeader(projectId, userId);

        if (request.getName() != null) project.updateName(request.getName());
        if (request.getTeamName() != null) project.updateTeamName(request.getTeamName());
        if (request.getDescription() != null) project.updateDescription(request.getDescription());
        if (request.getGrade() != null) project.updateGrade(request.getGrade());

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
                });
            }
        }
    }

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

        return savedProject.getId();
    }

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

    public void updateDescription(Long projectId, Long userId, UpdateProjectDescriptionRequest request) {
        Project project = projectQueryService.getProjectOrThrow(projectId);

        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw ProjectException.notMember();
        }

        if (request != null && request.description() != null) {
            project.updateDescription(request.description());
        }
    }

    public void transferLeader(Long projectId, Long userId, TransferLeaderRequest request) {
        if (userId.equals(request.newLeaderUserId())) {
            throw new ProjectException(HttpStatus.BAD_REQUEST, "본인에게 팀장을 양도할 수 없습니다.");
        }

        ProjectMember currentLeader = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(ProjectException::notMember);

        if (currentLeader.getRole() != ClientRole.LEADER) {
            throw ProjectException.notLeader();
        }

        ProjectMember newLeader = projectMemberRepository.findByProjectIdAndUserId(projectId, request.newLeaderUserId())
                .orElseThrow(ProjectException::notMember);

        currentLeader.changeRole(ClientRole.MEMBER);
        newLeader.changeRole(ClientRole.LEADER);
    }
}