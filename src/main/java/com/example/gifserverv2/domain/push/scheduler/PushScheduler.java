package com.example.gifserverv2.domain.push.scheduler;

import com.example.gifserverv2.domain.form.entity.CalendarEvent;
import com.example.gifserverv2.domain.form.entity.Form;
import com.example.gifserverv2.domain.form.repository.CalendarEventRepository;
import com.example.gifserverv2.domain.form.repository.FormRepository;
import com.example.gifserverv2.domain.form.repository.FormSubmitRepository;
import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.entity.ProjectMember;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.domain.push.entity.PushMessageTemplate;
import com.example.gifserverv2.domain.push.service.PushSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushScheduler {

    private final FormRepository formRepository;
    private final FormSubmitRepository formSubmitRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final PushSenderService pushSenderService;
    private final ProjectRepository projectRepository;
    private final CalendarEventRepository calendarEventRepository;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional(readOnly = true)
    public void sendFormDeadlineNotifications() {
        LocalDateTime now = LocalDateTime.now();

        List<Form> activeForms = formRepository.findAllByAnnouncedTrueAndDeadlineAfter(now);
        if (activeForms.isEmpty()) return;

        List<Project> allProjects = projectRepository.findAll();
        if (allProjects.isEmpty()) return;

        List<Long> formIds = activeForms.stream().map(Form::getId).toList();
        List<Long> projectIds = allProjects.stream().map(Project::getId).toList();

        Set<String> submittedKeySet = formSubmitRepository.findAllByFormIdIn(formIds).stream()
                .map(submit -> submit.getForm().getId() + "-" + submit.getProjectId())
                .collect(Collectors.toSet());

        Map<Long, List<Long>> projectUserIdsMap = projectMemberRepository.findAllByProjectIdIn(projectIds).stream()
                .collect(Collectors.groupingBy(
                        member -> member.getProject().getId(),
                        Collectors.mapping(ProjectMember::getUserId, Collectors.toList())
                ));

        for (Form form : activeForms) {
            long hoursLeft = Math.round(Duration.between(now, form.getDeadline()).toMinutes() / 60.0);

            String title;
            String body;

            if (hoursLeft == 1) {
                title = PushMessageTemplate.DEADLINE_1_HOUR.getTitle();
                body = PushMessageTemplate.DEADLINE_1_HOUR.formatBody(form.getTitle());
            } else if (hoursLeft == 5) {
                title = PushMessageTemplate.DEADLINE_5_HOURS.getTitle();
                body = PushMessageTemplate.DEADLINE_5_HOURS.formatBody(form.getTitle());
            } else if (hoursLeft == 24) {
                title = PushMessageTemplate.DEADLINE_1_DAY.getTitle();
                body = PushMessageTemplate.DEADLINE_1_DAY.formatBody(form.getTitle());
            } else if (hoursLeft == 72) {
                title = PushMessageTemplate.DEADLINE_3_DAYS.getTitle();
                body = PushMessageTemplate.DEADLINE_3_DAYS.formatBody(form.getTitle());
            } else if (hoursLeft == -1) {
                title = PushMessageTemplate.DEADLINE_MISSED.getTitle();
                body = PushMessageTemplate.DEADLINE_MISSED.formatBody(form.getTitle());
            } else {
                continue;
            }

            List<Project> targetProjects = (form.getTargetGrade() == null)
                    ? allProjects
                    : allProjects.stream().filter(p -> form.getTargetGrade().equals(p.getGrade())).toList();

            for (Project project : targetProjects) {
                String key = form.getId() + "-" + project.getId();

                if (!submittedKeySet.contains(key)) {
                    List<Long> targetUserIds = projectUserIdsMap.getOrDefault(project.getId(), List.of());
                    if (!targetUserIds.isEmpty()) {
                        pushSenderService.sendBulkNotifications(targetUserIds, title, body);
                    }
                }
            }
        }
    }

    @Scheduled(cron = "0 0 9 * * *")
    @Transactional(readOnly = true)
    public void sendCurrentPeriodScheduleNotifications() {
        LocalDate today = LocalDate.now();

        List<CalendarEvent> activeEvents = calendarEventRepository.findAllActiveEventsOnDate(today);
        if (activeEvents.isEmpty()) return;

        List<Long> projectIds = activeEvents.stream()
                .map(event -> event.getFormFieldAnswer().getFormSubmit().getProjectId())
                .distinct()
                .toList();

        Map<Long, Project> projectMap = projectRepository.findAllById(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, p -> p));

        Map<Long, List<Long>> projectUserIdsMap = projectMemberRepository.findAllByProjectIdIn(projectIds).stream()
                .collect(Collectors.groupingBy(
                        member -> member.getProject().getId(),
                        Collectors.mapping(ProjectMember::getUserId, Collectors.toList())
                ));

        for (CalendarEvent event : activeEvents) {
            Long projectId = event.getFormFieldAnswer().getFormSubmit().getProjectId();

            Project project = projectMap.get(projectId);
            if (project == null) continue;

            List<Long> memberUserIds = projectUserIdsMap.getOrDefault(projectId, List.of());
            if (memberUserIds.isEmpty()) continue;

            String startDateStr = event.getStartDate().toString();
            String endDateStr = event.getEndDate().toString();

            pushSenderService.sendBulkNotifications(
                    memberUserIds,
                    PushMessageTemplate.SCHEDULE_CURRENT_PERIOD.getTitle(),
                    PushMessageTemplate.SCHEDULE_CURRENT_PERIOD.formatBody(
                            project.getName(),
                            event.getEventName(),
                            startDateStr,
                            endDateStr
                    )
            );
        }
    }
}