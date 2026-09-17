package com.example.gifserverv2.domain.retrospective.service;

import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.entity.ProjectMember;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.domain.retrospective.dto.request.CreateRetrospectiveRequest;
import com.example.gifserverv2.domain.retrospective.dto.request.RelatedWorkRequest;
import com.example.gifserverv2.domain.retrospective.dto.request.RetrospectivePatchRequest;
import com.example.gifserverv2.domain.retrospective.dto.request.RetrospectiveSort;
import com.example.gifserverv2.domain.retrospective.dto.response.AuthorAffiliation;
import com.example.gifserverv2.domain.retrospective.dto.response.BookmarkToggleResponse;
import com.example.gifserverv2.domain.retrospective.dto.response.DetailRetrospectiveResponse;
import com.example.gifserverv2.domain.retrospective.dto.response.ListRetrospectiveResponse;
import com.example.gifserverv2.domain.retrospective.entity.Retrospective;
import com.example.gifserverv2.domain.retrospective.entity.RetrospectiveBookmark;
import com.example.gifserverv2.domain.retrospective.entity.RetrospectiveCategory;
import com.example.gifserverv2.domain.retrospective.entity.RetrospectiveRelatedWork;
import com.example.gifserverv2.domain.retrospective.entity.RetrospectiveVisibility;
import com.example.gifserverv2.domain.retrospective.exception.RetrospectiveException;
import com.example.gifserverv2.domain.retrospective.repository.RetrospectiveBookmarkRepository;
import com.example.gifserverv2.domain.retrospective.repository.RetrospectiveRepository;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RetrospectiveService {

    private static final int SUMMARY_MAX_LENGTH = 150;

    private final RetrospectiveRepository retrospectiveRepository;
    private final RetrospectiveBookmarkRepository retrospectiveBookmarkRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long create(Long userId, CreateRetrospectiveRequest request) {
        Retrospective retrospective = Retrospective.builder()
                .userId(userId)
                .title(request.title())
                .content(request.content())
                .summary(summarize(request.content()))
                .category(request.category())
                .visibility(request.visibility())
                .isDraft(false)
                .relatedWorks(toEntities(request.relatedWorks()))
                .build();

        return retrospectiveRepository.save(retrospective).getId();
    }

    @Transactional
    public void update(Long userId, Long id, RetrospectivePatchRequest request) {
        Retrospective retrospective = getOwnedOrThrow(id, userId, false);

        retrospective.applyPatch(
                request.title(),
                request.content(),
                request.category(),
                request.visibility(),
                request.relatedWorks() != null ? toEntities(request.relatedWorks()) : null,
                request.content() != null ? summarize(request.content()) : null
        );
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Retrospective retrospective = getOwnedOrThrow(id, userId, false);
        retrospectiveBookmarkRepository.deleteAllByRetrospectiveId(id);
        retrospectiveRepository.delete(retrospective);
    }

    @Transactional(readOnly = true)
    public Page<ListRetrospectiveResponse> getList(
            Long userId,
            RetrospectiveCategory category,
            String keyword,
            RetrospectiveSort sort,
            Pageable pageable
    ) {
        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), resolveSort(sort));
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        Page<Retrospective> retrospectives = retrospectiveRepository.search(
                userId, RetrospectiveVisibility.PUBLIC, category, normalizedKeyword, sorted
        );

        List<Retrospective> content = retrospectives.getContent();
        List<Long> authorIds = authorIds(content);
        Map<Long, AuthorAffiliation> affiliationMap = getAuthorAffiliationMap(authorIds);
        Map<Long, String> userNameMap = getUserNameMap(authorIds);
        Set<Long> bookmarkedIds = getBookmarkedIds(userId, ids(content));

        return retrospectives.map(r -> ListRetrospectiveResponse.from(
                r,
                affiliationMap.getOrDefault(r.getUserId(), AuthorAffiliation.empty()),
                userNameMap.get(r.getUserId()),
                bookmarkedIds.contains(r.getId())
        ));
    }

    @Transactional(readOnly = true)
    public DetailRetrospectiveResponse getDetail(Long userId, Long id) {
        Retrospective retrospective = retrospectiveRepository.findById(id)
                .filter(r -> !r.isDraft())
                .orElseThrow(RetrospectiveException::notFound);

        if (!retrospective.isPublicPost() && !retrospective.isOwnedBy(userId)) {
            throw RetrospectiveException.accessDenied();
        }

        return toDetailResponse(retrospective, userId);
    }

    @Transactional
    public BookmarkToggleResponse toggleBookmark(Long userId, Long id) {
        Retrospective retrospective = retrospectiveRepository.findById(id)
                .filter(r -> !r.isDraft())
                .orElseThrow(RetrospectiveException::notFound);

        if (!retrospective.isPublicPost()) {
            throw RetrospectiveException.accessDenied();
        }
        if (retrospective.isOwnedBy(userId)) {
            throw RetrospectiveException.cannotBookmarkOwn();
        }

        Optional<RetrospectiveBookmark> existing = retrospectiveBookmarkRepository.findByRetrospectiveIdAndUserId(id, userId);
        boolean nowBookmarked;
        if (existing.isPresent()) {
            retrospectiveBookmarkRepository.delete(existing.get());
            retrospective.decreaseBookmarkCount();
            nowBookmarked = false;
        } else {
            retrospectiveBookmarkRepository.save(RetrospectiveBookmark.builder()
                    .retrospectiveId(id)
                    .userId(userId)
                    .build());
            retrospective.increaseBookmarkCount();
            nowBookmarked = true;
        }

        return new BookmarkToggleResponse(retrospective.getBookmarkCount(), nowBookmarked);
    }

    @Transactional
    public Long createDraft(Long userId, RetrospectivePatchRequest request) {
        Retrospective draft = Retrospective.builder()
                .userId(userId)
                .title(request.title())
                .content(request.content())
                .summary(request.content() != null ? summarize(request.content()) : null)
                .category(request.category())
                .visibility(request.visibility() != null ? request.visibility() : RetrospectiveVisibility.PRIVATE)
                .isDraft(true)
                .relatedWorks(toEntities(request.relatedWorks()))
                .build();

        return retrospectiveRepository.save(draft).getId();
    }

    @Transactional
    public void updateDraft(Long userId, Long id, RetrospectivePatchRequest request) {
        Retrospective draft = getOwnedOrThrow(id, userId, true);

        draft.applyPatch(
                request.title(),
                request.content(),
                request.category(),
                request.visibility(),
                request.relatedWorks() != null ? toEntities(request.relatedWorks()) : null,
                request.content() != null ? summarize(request.content()) : null
        );
    }

    @Transactional(readOnly = true)
    public List<ListRetrospectiveResponse> getMyDrafts(Long userId) {
        List<Retrospective> drafts = retrospectiveRepository.findAllByIsDraftTrueAndUserIdOrderByUpdatedAtDesc(userId);
        AuthorAffiliation affiliation = getAuthorAffiliation(userId);
        String userName = getUserName(userId);

        return drafts.stream()
                .map(r -> ListRetrospectiveResponse.from(r, affiliation, userName, false))
                .toList();
    }

    @Transactional
    public void deleteDraft(Long userId, Long id) {
        Retrospective draft = getOwnedOrThrow(id, userId, true);
        retrospectiveRepository.delete(draft);
    }

    private Retrospective getOwnedOrThrow(Long id, Long userId, boolean expectDraft) {
        Retrospective retrospective = retrospectiveRepository.findById(id)
                .filter(r -> r.isDraft() == expectDraft)
                .orElseThrow(expectDraft ? RetrospectiveException::draftNotFound : RetrospectiveException::notFound);

        if (!retrospective.isOwnedBy(userId)) {
            throw RetrospectiveException.forbidden();
        }
        return retrospective;
    }

    private DetailRetrospectiveResponse toDetailResponse(Retrospective retrospective, Long viewerId) {
        AuthorAffiliation affiliation = getAuthorAffiliation(retrospective.getUserId());
        String authorName = getUserName(retrospective.getUserId());
        boolean isBookmarked = retrospectiveBookmarkRepository.existsByRetrospectiveIdAndUserId(retrospective.getId(), viewerId);
        boolean isMine = retrospective.isOwnedBy(viewerId);

        return DetailRetrospectiveResponse.from(retrospective, affiliation, authorName, isBookmarked, isMine);
    }

    private Sort resolveSort(RetrospectiveSort sort) {
        RetrospectiveSort effective = sort != null ? sort : RetrospectiveSort.LATEST;
        return switch (effective) {
            case LATEST -> Sort.by("createdAt").descending();
            case OLDEST -> Sort.by("createdAt").ascending();
            case BOOKMARK -> Sort.by("bookmarkCount").descending();
        };
    }

    private List<RetrospectiveRelatedWork> toEntities(List<RelatedWorkRequest> requests) {
        if (requests == null) {
            return new ArrayList<>();
        }
        return requests.stream()
                .map(r -> RetrospectiveRelatedWork.builder()
                        .type(r.type())
                        .number(r.number())
                        .title(r.title())
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private String summarize(String content) {
        if (content == null) {
            return null;
        }
        String plain = content
                .replaceAll("(?m)^#{1,6}\\s*", "")
                .replaceAll("[*_`>]", "")
                .replaceAll("!?\\[[^]]*]\\([^)]*\\)", "")
                .replaceAll("\\s+", " ")
                .trim();
        if (plain.length() <= SUMMARY_MAX_LENGTH) {
            return plain;
        }
        return plain.substring(0, SUMMARY_MAX_LENGTH).trim() + "…";
    }

    private List<Long> authorIds(List<Retrospective> retrospectives) {
        return retrospectives.stream().map(Retrospective::getUserId).distinct().toList();
    }

    private List<Long> ids(List<Retrospective> retrospectives) {
        return retrospectives.stream().map(Retrospective::getId).toList();
    }

    private AuthorAffiliation getAuthorAffiliation(Long userId) {
        return getAuthorAffiliationMap(List.of(userId)).getOrDefault(userId, AuthorAffiliation.empty());
    }

    private Map<Long, AuthorAffiliation> getAuthorAffiliationMap(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }

        List<ProjectMember> members = projectMemberRepository.findAllByUserIdIn(userIds);
        Map<Long, ProjectMember> memberByUserId = members.stream()
                .collect(Collectors.toMap(ProjectMember::getUserId, m -> m, (a, b) -> a));

        Set<Long> projectIds = members.stream()
                .map(m -> m.getProject().getId())
                .collect(Collectors.toSet());
        Map<Long, Project> projectById = projectRepository.findAllById(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, p -> p));

        Map<Long, AuthorAffiliation> result = new HashMap<>();
        for (Long userId : userIds) {
            ProjectMember member = memberByUserId.get(userId);
            if (member == null) {
                result.put(userId, AuthorAffiliation.empty());
                continue;
            }
            Project project = projectById.get(member.getProject().getId());
            result.put(userId, new AuthorAffiliation(
                    project != null ? project.getGrade() : null,
                    project != null ? project.getClassNo() : null,
                    project != null ? project.getTeamNo() : null,
                    member.getPart()
            ));
        }
        return result;
    }

    private String getUserName(Long userId) {
        return userRepository.findById(userId)
                .map(UserEntity::getName)
                .orElse(null);
    }

    private Map<Long, String> getUserNameMap(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, UserEntity::getName));
    }

    private Set<Long> getBookmarkedIds(Long userId, List<Long> retrospectiveIds) {
        if (retrospectiveIds.isEmpty()) {
            return Set.of();
        }
        return retrospectiveBookmarkRepository.findAllByUserIdAndRetrospectiveIdIn(userId, retrospectiveIds).stream()
                .map(RetrospectiveBookmark::getRetrospectiveId)
                .collect(Collectors.toSet());
    }
}
