package com.example.gifserverv2.domain.project.service;

import com.example.gifserverv2.domain.project.dto.response.DetailProjectResponse;
import com.example.gifserverv2.domain.project.dto.response.ListProjectResponse;
import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.exception.ProjectException;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

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
        return DetailProjectResponse.from(getProjectOrThrow(projectId));
    }

    public Project getProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(ProjectException::notFound);
    }
}