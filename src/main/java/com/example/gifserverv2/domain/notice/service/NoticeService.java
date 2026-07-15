package com.example.gifserverv2.domain.notice.service;

import com.example.gifserverv2.domain.notice.dto.request.CreateNoticeRequest;
import com.example.gifserverv2.domain.notice.dto.response.DetailNoticeResponse;
import com.example.gifserverv2.domain.notice.dto.response.ListNoticeResponse;
import com.example.gifserverv2.domain.notice.entity.Notice;
import com.example.gifserverv2.domain.notice.exception.NoticeException;
import com.example.gifserverv2.domain.notice.repository.NoticeRepository;
import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.global.discord.DiscordBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private static final int TITLE_MAX_LENGTH = 100;
    private static final int CONTENT_MAX_LENGTH = 2000;

    private final NoticeRepository noticeRepository;
    private final ProjectRepository projectRepository;
    private final DiscordBotService discordBotService;

    @Transactional
    public Long createNotice(Long userId, CreateNoticeRequest request) {
        validateLength(request.title(), request.content());

        Notice notice = Notice.builder()
                .title(request.title())
                .content(request.content())
                .createdByUserId(userId)
                .targetGrades(request.targetGrades() != null ? request.targetGrades() : new ArrayList<>())
                .targetProjectIds(request.targetProjectIds() != null ? request.targetProjectIds() : new ArrayList<>())
                .build();

        Notice saved = noticeRepository.save(notice);
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        sendDiscordNotification(saved);
                    }
                }
        );

        return saved.getId();
    }

    public List<ListNoticeResponse> getAllNotices() {
        List<Notice> notices = noticeRepository.findAllByOrderByCreatedAtDesc();
        Map<Long, String> teamNameMap = getTeamNameMap(notices);

        return notices.stream()
                .map(notice -> ListNoticeResponse.from(notice, resolveTeamNames(notice, teamNameMap)))
                .toList();
    }

    public DetailNoticeResponse getNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(NoticeException::notFound);

        Map<Long, String> teamNameMap = getTeamNameMap(List.of(notice));

        return DetailNoticeResponse.from(notice, resolveTeamNames(notice, teamNameMap));
    }

    @Transactional
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(NoticeException::notFound);
        noticeRepository.delete(notice);
    }

    private void validateLength(String title, String content) {
        if (title != null && title.length() > TITLE_MAX_LENGTH) {
            throw NoticeException.titleTooLong();
        }
        if (content != null && content.length() > CONTENT_MAX_LENGTH) {
            throw NoticeException.contentTooLong();
        }
    }

    private Map<Long, String> getTeamNameMap(List<Notice> notices) {
        Set<Long> projectIds = notices.stream()
                .flatMap(n -> n.getTargetProjectIds().stream())
                .collect(Collectors.toSet());

        if (projectIds.isEmpty()) {
            return Map.of();
        }

        return projectRepository.findAllById(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, Project::getTeamName));
    }

    private List<String> resolveTeamNames(Notice notice, Map<Long, String> teamNameMap) {
        return notice.getTargetProjectIds().stream()
                .map(teamNameMap::get)
                .filter(name -> name != null)
                .toList();
    }

    private void sendDiscordNotification(Notice notice) {
        Map<Long, String> teamNameMap = getTeamNameMap(List.of(notice));
        List<String> teamNames = resolveTeamNames(notice, teamNameMap);

        StringBuilder tagLine = new StringBuilder();
        for (Integer grade : notice.getTargetGrades()) {
            tagLine.append("`").append(grade).append("학년` ");
        }
        for (String teamName : teamNames) {
            tagLine.append("`").append(teamName).append("` ");
        }

        StringBuilder message = new StringBuilder();
        message.append("📢 **").append(notice.getTitle()).append("**\n");
        if (!tagLine.isEmpty()) {
            message.append(tagLine).append("\n\n");
        }
        message.append(notice.getContent());

        discordBotService.sendNoticeMessage(message.toString());
    }
}