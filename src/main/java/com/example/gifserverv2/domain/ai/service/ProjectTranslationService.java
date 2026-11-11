package com.example.gifserverv2.domain.ai.service;

import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.event.ProjectTranslationRequestedEvent;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// 프로젝트 소개를 일본어로 번역해 저장한다. 요청한 쪽의 응답이 늦어지지 않도록 비동기로 처리한다.
// 진행 중·실패 기록은 서버 메모리에 두므로 서버를 여러 대로 늘리면 서버마다 따로 관리된다.
@Slf4j
@Service
public class ProjectTranslationService {

    // 같은 소개의 번역이 실패하면 이 시간 동안은 다시 시도하지 않는다. (장애·키 오류 시 조회마다 OpenAI 를 부르지 않게)
    static final Duration RETRY_AFTER_FAILURE = Duration.ofMinutes(10);
    static final int MAX_DESCRIPTION_JA_LENGTH = 2_000;

    private final ProjectRepository projectRepository;
    private final AiTranslationService translationService;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    private final Set<Long> inProgress = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<Long, Failure> failures = new ConcurrentHashMap<>();

    private record Source(String description, String hash, String name, String teamName) {}

    private record Failure(String hash, Instant at) {}

    @Autowired
    public ProjectTranslationService(ProjectRepository projectRepository,
                                     AiTranslationService translationService,
                                     PlatformTransactionManager transactionManager) {
        this(projectRepository, translationService, transactionManager, Clock.systemUTC());
    }

    ProjectTranslationService(ProjectRepository projectRepository,
                              AiTranslationService translationService,
                              PlatformTransactionManager transactionManager,
                              Clock clock) {
        this.projectRepository = projectRepository;
        this.translationService = translationService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.clock = clock;
    }

    // 트랜잭션 안에서 발행되면 커밋된 뒤에 번역한다. (커밋 전 소개를 읽지 않게)
    @Async
    @TransactionalEventListener(fallbackExecution = true)
    public void onTranslationRequested(ProjectTranslationRequestedEvent event) {
        translateDescription(event.projectId());
    }

    void translateDescription(Long projectId) {
        if (!translationService.isAvailable() || !inProgress.add(projectId)) {
            return;
        }
        try {
            Source source = transactionTemplate.execute(status -> loadSourceToTranslate(projectId));
            if (source == null || recentlyFailed(projectId, source.hash())) {
                return;
            }

            String translated;
            try {
                translated = translationService.translateToJapanese(
                        source.description(), List.of(source.name(), source.teamName()));
            } catch (Exception e) {
                failures.put(projectId, new Failure(source.hash(), clock.instant()));
                log.warn("[Translation] 프로젝트 {} 소개 일본어 번역에 실패했습니다: {}", projectId, e.getMessage());
                return;
            }
            if (translated.length() > MAX_DESCRIPTION_JA_LENGTH) {
                failures.put(projectId, new Failure(source.hash(), clock.instant()));
                log.warn("[Translation] 프로젝트 {} 소개 번역 결과가 {}자를 넘어 저장하지 않습니다.", projectId, MAX_DESCRIPTION_JA_LENGTH);
                return;
            }

            failures.remove(projectId);
            transactionTemplate.executeWithoutResult(status -> saveIfSourceUnchanged(projectId, source.hash(), translated));
        } finally {
            inProgress.remove(projectId);
        }
    }

    // 번역할 필요가 없으면(소개가 비었거나 이미 지금 소개의 번역이 있으면) null
    private Source loadSourceToTranslate(Long projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null || project.getDescription() == null || project.getDescription().isBlank()
                || project.currentDescriptionJa() != null) {
            return null;
        }
        return new Source(project.getDescription(), project.descriptionHash(), project.getName(), project.getTeamName());
    }

    private boolean recentlyFailed(Long projectId, String hash) {
        Failure failure = failures.get(projectId);
        return failure != null
                && failure.hash().equals(hash)
                && failure.at().plus(RETRY_AFTER_FAILURE).isAfter(clock.instant());
    }

    // 번역하는 사이 소개가 또 바뀌었으면 옛 소개의 번역이므로 저장하지 않는다.
    private void saveIfSourceUnchanged(Long projectId, String sourceHash, String translated) {
        projectRepository.findById(projectId)
                .filter(project -> project.getDescription() != null && project.descriptionHash().equals(sourceHash))
                .ifPresent(project -> project.updateDescriptionJa(translated, sourceHash));
    }
}
