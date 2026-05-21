package com.example.gifserverv2.project.service;

import com.example.gifserverv2.project.dto.request.*;
import com.example.gifserverv2.project.dto.response.*;
import com.example.gifserverv2.project.entity.Project;
import com.example.gifserverv2.project.entity.ProjectMember;
import com.example.gifserverv2.project.exception.ProjectException;
import com.example.gifserverv2.project.repository.ProjectMemberRepository;
import com.example.gifserverv2.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Transactional
    public Long createProject(Long leaderId, ProjectCreateRequest request) {
        Project project = Project.builder()
                .name(request.name())
                .teamName(request.teamName())
                .description(request.description())
                .build();
        projectRepository.save(project);

        projectMemberRepository.save(ProjectMember.builder()
                .project(project)
                .userId(leaderId)
                .role(ProjectMember.MemberRole.LEADER)
                .build());

        if (request.memberIds() != null) {
            request.memberIds().stream()
                    .filter(id -> !id.equals(leaderId)) // 팀장 중복 방지
                    .forEach(memberId ->
                            projectMemberRepository.save(ProjectMember.builder()
                                    .project(project)
                                    .userId(memberId)
                                    .role(ProjectMember.MemberRole.MEMBER)
                                    .build())
                    );
        }

        return project.getId();
    }

    public List<ProjectListResponse> getAllProjects() {
        return projectRepository.findAllWithMembers().stream()
                .map(ProjectListResponse::from)
                .toList();
    }

    public List<ProjectListResponse> getMyProjects(Long userId) {
        return projectMemberRepository.findAllByUserId(userId).stream()
                .map(m -> ProjectListResponse.from(m.getProject()))
                .toList();
    }

    public ProjectDetailResponse getProject(Long projectId) {
        Project project = getProjectOrThrow(projectId);
        return ProjectDetailResponse.from(project);
    }

    @Transactional
    public void updateName(Long projectId, Long userId, ProjectUpdateNameRequest request) {
        Project project = getProjectOrThrow(projectId);
        validateLeader(projectId, userId);
        project.updateName(request.name());
    }

    @Transactional
    public void updateTeamName(Long projectId, Long userId, ProjectUpdateTeamNameRequest request) {
        Project project = getProjectOrThrow(projectId);
        validateLeader(projectId, userId);
        project.updateTeamName(request.teamName());
    }

    @Transactional
    public void updateDescription(Long projectId, Long userId, ProjectUpdateDescriptionRequest request) {
        Project project = getProjectOrThrow(projectId);
        validateLeader(projectId, userId);
        project.updateDescription(request.description());
    }

    @Transactional
    public void updateMembers(Long projectId, Long userId, ProjectUpdateMembersRequest request) {
        getProjectOrThrow(projectId);
        validateLeader(projectId, userId);

        if (request.addMemberIds() != null) {
            request.addMemberIds().forEach(memberId -> {
                if (projectMemberRepository.existsByProjectIdAndUserId(projectId, memberId)) {
                    throw ProjectException.alreadyMember();
                }
                Project project = projectRepository.findById(projectId).orElseThrow(ProjectException::notFound);
                projectMemberRepository.save(ProjectMember.builder()
                        .project(project)
                        .userId(memberId)
                        .role(ProjectMember.MemberRole.MEMBER)
                        .build());
            });
        }

        if (request.removeMemberIds() != null) {
            request.removeMemberIds().forEach(memberId -> {
                ProjectMember member = projectMemberRepository
                        .findByProjectIdAndUserId(projectId, memberId)
                        .orElseThrow(ProjectException::notMember);

                if (member.getRole() == ProjectMember.MemberRole.LEADER) {
                    throw ProjectException.cannotRemoveLeader();
                }
                projectMemberRepository.delete(member);
            });
        }
    }

    private Project getProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(ProjectException::notFound);
    }

    private void validateLeader(Long projectId, Long userId) {
        ProjectMember member = projectMemberRepository
                .findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(ProjectException::notMember);

        if (member.getRole() != ProjectMember.MemberRole.LEADER) {
            throw ProjectException.notLeader();
        }
    }
}
