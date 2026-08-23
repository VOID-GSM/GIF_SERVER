package com.example.gifserverv2.domain.retrospect.service;

import com.example.gifserverv2.domain.project.exception.ProjectException;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import com.example.gifserverv2.domain.retrospect.dto.request.WriteRetrospectRequest;
import com.example.gifserverv2.domain.retrospect.dto.response.DetailRetrospectResponse;
import com.example.gifserverv2.domain.retrospect.dto.response.ListRetrospectResponse;
import com.example.gifserverv2.domain.retrospect.entity.Retrospect;
import com.example.gifserverv2.domain.retrospect.exception.RetrospectException;
import com.example.gifserverv2.domain.retrospect.repository.RetrospectRepository;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RetrospectService {

    private static final int TITLE_MAX_LENGTH = 100;

    private final RetrospectRepository retrospectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long writeOrUpdate(Long projectId, Long userId, WriteRetrospectRequest request) {
        validateMember(projectId, userId);
        validateTitle(request.title());

        Retrospect retrospect = retrospectRepository.findByProjectIdAndUserId(projectId, userId)
                .orElse(null);

        if (retrospect == null) {
            Retrospect newRetrospect = Retrospect.builder()
                    .projectId(projectId)
                    .userId(userId)
                    .title(request.title())
                    .content(request.content())
                    .build();
            return retrospectRepository.save(newRetrospect).getId();
        }

        retrospect.update(request.title(), request.content());
        return retrospect.getId();
    }

    @Transactional(readOnly = true)
    public DetailRetrospectResponse getMy(Long projectId, Long userId) {
        validateMember(projectId, userId);

        Retrospect retrospect = retrospectRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(RetrospectException::notFound);

        String userName = getUserName(userId);
        return DetailRetrospectResponse.from(retrospect, userName);
    }

    @Transactional(readOnly = true)
    public List<ListRetrospectResponse> getTeamRetrospects(Long projectId, Long userId) {
        validateMember(projectId, userId);

        List<Retrospect> retrospects = retrospectRepository.findAllByProjectIdOrderByUpdatedAtDesc(projectId);
        Map<Long, String> userNameMap = getUserNameMap(retrospects);

        return retrospects.stream()
                .map(r -> ListRetrospectResponse.from(r, userNameMap.get(r.getUserId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public DetailRetrospectResponse getDetail(Long projectId, Long userId, Long retrospectId) {
        validateMember(projectId, userId);

        Retrospect retrospect = retrospectRepository.findById(retrospectId)
                .orElseThrow(RetrospectException::notFound);

        if (!retrospect.getProjectId().equals(projectId)) {
            throw RetrospectException.notFound();
        }

        String userName = getUserName(retrospect.getUserId());
        return DetailRetrospectResponse.from(retrospect, userName);
    }

    @Transactional
    public void delete(Long projectId, Long userId) {
        validateMember(projectId, userId);

        Retrospect retrospect = retrospectRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(RetrospectException::notFound);

        retrospectRepository.delete(retrospect);
    }

    private void validateMember(Long projectId, Long userId) {
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw ProjectException.notMember();
        }
    }

    private void validateTitle(String title) {
        if (title != null && title.length() > TITLE_MAX_LENGTH) {
            throw RetrospectException.titleTooLong();
        }
    }

    private String getUserName(Long userId) {
        return userRepository.findById(userId)
                .map(UserEntity::getName)
                .orElse(null);
    }

    private Map<Long, String> getUserNameMap(List<Retrospect> retrospects) {
        Set<Long> userIds = retrospects.stream()
                .map(Retrospect::getUserId)
                .collect(Collectors.toSet());

        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, UserEntity::getName));
    }
}