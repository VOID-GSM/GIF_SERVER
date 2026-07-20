package com.example.gifserverv2.domain.push.scheduler;

import com.example.gifserverv2.domain.form.entity.Form;
import com.example.gifserverv2.domain.form.repository.FormRepository;
import com.example.gifserverv2.domain.form.repository.FormSubmitRepository;
import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.domain.push.entity.PushMessageTemplate;
import com.example.gifserverv2.domain.push.service.PushSenderService;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushScheduler {

    private final FormRepository formRepository;
    private final FormSubmitRepository formSubmitRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final PushSenderService pushSenderService;
    private final ProjectRepository projectRepository;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional(readOnly = true)
    public void sendFormDeadlineNotifications() {
        LocalDateTime now = LocalDateTime.now();

        List<Form> activeForms = formRepository.findAllByAnnouncedTrueAndDeadlineAfter(now);

        for (Form form : activeForms) {
            long hoursLeft = Duration.between(now, form.getDeadline()).toHours();

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
                    ? projectRepository.findAll()
                    : projectRepository.findByGrade(form.getTargetGrade());

            for (Project project : targetProjects) {
                boolean isSubmitted = formSubmitRepository.existsByFormIdAndProjectId(form.getId(), project.getId());

                if (!isSubmitted) {
                    List<Long> targetUserIds = projectMemberRepository.findUserIdsByProjectId(project.getId());
                    for (Long userId : targetUserIds) {
                        pushSenderService.sendNotification(userId, title, body);
                    }
                }
            }
        }
    }
}