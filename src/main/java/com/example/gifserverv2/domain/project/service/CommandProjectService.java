package com.example.gifserverv2.domain.project.service;

import com.example.gifserverv2.domain.project.dto.request.*;
import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.entity.ProjectMember;
import com.example.gifserverv2.domain.project.exception.ProjectException;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CommandProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final QueryProjectService projectQueryService;

    @Value("${file.upload-dir:uploads/logos}")
    private String uploadDir;

    public Long createProject(Long leaderId, CreateProjectRequest request) {
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
                    .filter(id -> !id.equals(leaderId))
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

    public void updateProject(Long projectId, Long userId, UpdateProjectRequest request, MultipartFile logo) {
        Project project = projectQueryService.getProjectOrThrow(projectId);
        validateLeader(projectId, userId);

        if (request.getName() != null) project.updateName(request.getName());
        if (request.getTeamName() != null) project.updateTeamName(request.getTeamName());
        if (request.getDescription() != null) project.updateDescription(request.getDescription());

        if (logo != null && !logo.isEmpty()) {
            try {
                Path dir = Paths.get(uploadDir);
                if (!Files.exists(dir)) Files.createDirectories(dir);

                if (project.getLogoPath() != null) {
                    Files.deleteIfExists(dir.resolve(project.getLogoPath()));
                }

                String originalFilename = logo.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }

                String fileName = UUID.randomUUID() + extension;
                Path filePath = dir.resolve(fileName).normalize();
                logo.transferTo(filePath.toAbsolutePath().toFile());

                project.updateLogoPath(fileName);

            } catch (IOException e) {
                throw new RuntimeException("로고 업로드 중 오류가 발생했습니다.", e);
            }
        }

        if (request.getAddMemberIds() != null) {
            request.getAddMemberIds().forEach(memberId -> {
                if (projectMemberRepository.existsByProjectIdAndUserId(projectId, memberId)) {
                    throw ProjectException.alreadyMember();
                }
                projectMemberRepository.save(ProjectMember.builder()
                        .project(project)
                        .userId(memberId)
                        .role(ProjectMember.MemberRole.MEMBER)
                        .build());
            });
        }

        if (request.getRemoveMemberIds() != null) {
            request.getRemoveMemberIds().forEach(memberId -> {
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

    public void uploadLogo(Long projectId, Long userId, MultipartFile file) {
        Project project = projectQueryService.getProjectOrThrow(projectId);
        validateLeader(projectId, userId);

        try {
            Path dir = Paths.get(uploadDir);
            if (!Files.exists(dir)) Files.createDirectories(dir);

            if (project.getLogoPath() != null) {
                Files.deleteIfExists(Paths.get(project.getLogoPath()));
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = dir.resolve(fileName);
            file.transferTo(filePath.toFile());

            project.updateLogoPath(filePath.toString());

        } catch (IOException e) {
            throw new RuntimeException("로고 업로드 중 오류가 발생했습니다.", e);
        }
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
