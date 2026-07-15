package com.example.gifserverv2.domain.project.service;

import com.example.gifserverv2.domain.project.dto.request.CreateProjectNoteRequest;
import com.example.gifserverv2.domain.project.dto.response.GetProjectNoteResponse;
import com.example.gifserverv2.domain.project.entity.ProjectNote;
import com.example.gifserverv2.domain.project.repository.ProjectNoteRepository;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProjectNoteService {

    private final ProjectNoteRepository projectNoteRepository;
    private final UserRepository userRepository;
    private final QueryProjectService projectQueryService;

    @Transactional
    public void writeOrUpdateNote(Long projectId, Long userId, CreateProjectNoteRequest request) {
        projectQueryService.getProjectOrThrow(projectId);

        projectNoteRepository.findByProjectIdAndUserId(projectId, userId)
                .ifPresentOrElse(
                        note -> note.updateContent(request.content()),
                        () -> {
                            ProjectNote newNote = ProjectNote.builder()
                                    .projectId(projectId)
                                    .userId(userId)
                                    .content(request.content())
                                    .build();
                            projectNoteRepository.save(newNote);
                        }
                );
    }

    @Transactional(readOnly = true)
    public GetProjectNoteResponse getMyNote(Long projectId, Long userId) {
        projectQueryService.getProjectOrThrow(projectId);

        String content = projectNoteRepository.findByProjectIdAndUserId(projectId, userId)
                .map(ProjectNote::getContent)
                .orElse("");

        return GetProjectNoteResponse.of(projectId, content);
    }
}
