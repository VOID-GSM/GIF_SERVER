package com.example.gifserverv2.domain.project.service;

import com.example.gifserverv2.domain.project.dto.response.DetailProjectResponse;
import com.example.gifserverv2.domain.project.dto.response.ListProjectResponse;
import com.example.gifserverv2.domain.project.dto.response.UserSearchResponse;
import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.entity.TeamMember;
import com.example.gifserverv2.domain.project.exception.ProjectException;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.domain.project.repository.UserSearchRepository;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserSearchRepository userSearchRepository;
    private final UserRepository userRepository;

    public List<UserSearchResponse> searchUsers(String keyword) {
        return userSearchRepository
                .findByNameContainingOrStudentNumberContaining(keyword, keyword)
                .stream()
                .map(UserSearchResponse::from)
                .toList();
    }

    public List<ListProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(ListProjectResponse::from)
                .toList();
    }

    public List<ListProjectResponse> getMyProjects(Long userId) {
        return projectMemberRepository.findAllByUserId(userId).stream()
                .map(m -> ListProjectResponse.from(m.getProject()))
                .toList();
    }

    public List<ListProjectResponse> getProjectsByGrade(Integer grade) {
        if (grade == null) return getAllProjects();
        return projectRepository.findByGrade(grade).stream()
                .map(ListProjectResponse::from)
                .toList();
    }

    public DetailProjectResponse getProject(Long projectId) {
        Project project = getProjectOrThrow(projectId);
        List<Long> memberIds = project.getMembers().stream()
                .map(TeamMember::getUserId)
                .toList();

        Map<Long, UserEntity> userMap = userRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));

        return DetailProjectResponse.from(project, userMap);
    }

    public Project getProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(ProjectException::notFound);
    }
}