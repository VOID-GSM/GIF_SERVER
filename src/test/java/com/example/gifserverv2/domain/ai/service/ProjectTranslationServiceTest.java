package com.example.gifserverv2.domain.ai.service;

import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ProjectTranslationServiceTest {

    private static final String DESCRIPTION = "GIF는 아이디어 페스티벌 운영 플랫폼입니다.";
    private static final String DESCRIPTION_JA = "GIFはアイデアフェスティバル運営プラットフォームです。";

    @Autowired ProjectRepository projectRepository;
    @Autowired PlatformTransactionManager transactionManager;

    AiTranslationService translationService = mock(AiTranslationService.class);
    MutableClock clock = new MutableClock(Instant.parse("2026-11-11T00:00:00Z"));
    ProjectTranslationService service;
    Long projectId;

    @BeforeEach
    void setUp() {
        service = new ProjectTranslationService(projectRepository, translationService, transactionManager, clock);
        projectId = projectRepository.save(Project.builder()
                .name("GIF").teamName("VOID").description(DESCRIPTION).build()).getId();
        given(translationService.isAvailable()).willReturn(true);
    }

    @AfterEach
    void tearDown() {
        projectRepository.deleteAll();
    }

    private Project reload() {
        return projectRepository.findById(projectId).orElseThrow();
    }

    @Test
    void 소개를_번역해_저장하고_프로젝트명_팀명은_그대로_두도록_요청한다() {
        given(translationService.translateToJapanese(DESCRIPTION, List.of("GIF", "VOID"))).willReturn(DESCRIPTION_JA);

        service.translateDescription(projectId);

        assertThat(reload().currentDescriptionJa()).isEqualTo(DESCRIPTION_JA);
    }

    @Test
    void 이미_번역된_소개는_다시_번역하지_않는다() {
        given(translationService.translateToJapanese(anyString(), anyList())).willReturn(DESCRIPTION_JA);

        service.translateDescription(projectId);
        service.translateDescription(projectId);

        verify(translationService, times(1)).translateToJapanese(anyString(), anyList());
    }

    @Test
    void 소개가_바뀌면_새_소개로_다시_번역한다() {
        given(translationService.translateToJapanese(eq(DESCRIPTION), anyList())).willReturn(DESCRIPTION_JA);
        given(translationService.translateToJapanese(eq("새 소개입니다."), anyList())).willReturn("新しい紹介です。");
        service.translateDescription(projectId);

        Project project = reload();
        project.updateDescription("새 소개입니다.");
        projectRepository.save(project);
        service.translateDescription(projectId);

        assertThat(reload().currentDescriptionJa()).isEqualTo("新しい紹介です。");
    }

    @Test
    void 번역하는_사이_소개가_바뀌면_옛_번역을_저장하지_않는다() {
        given(translationService.translateToJapanese(anyString(), anyList())).willAnswer(invocation -> {
            Project project = reload();
            project.updateDescription("번역 중에 바뀐 소개");
            projectRepository.save(project);
            return DESCRIPTION_JA;
        });

        service.translateDescription(projectId);

        Project project = reload();
        assertThat(project.getDescriptionJa()).isNull();
        assertThat(project.currentDescriptionJa()).isNull();
    }

    @Test
    void 번역에_실패하면_저장하지_않고_잠시_동안은_다시_시도하지_않는다() {
        given(translationService.translateToJapanese(anyString(), anyList()))
                .willThrow(new ResourceAccessException("timeout"))
                .willReturn(DESCRIPTION_JA);

        service.translateDescription(projectId);
        service.translateDescription(projectId);

        assertThat(reload().currentDescriptionJa()).isNull();
        verify(translationService, times(1)).translateToJapanese(anyString(), anyList());

        clock.advance(ProjectTranslationService.RETRY_AFTER_FAILURE.plusSeconds(1));
        service.translateDescription(projectId);

        assertThat(reload().currentDescriptionJa()).isEqualTo(DESCRIPTION_JA);
    }

    @Test
    void 번역_결과가_컬럼_길이를_넘으면_저장하지_않는다() {
        given(translationService.translateToJapanese(anyString(), anyList()))
                .willReturn("あ".repeat(ProjectTranslationService.MAX_DESCRIPTION_JA_LENGTH + 1));

        service.translateDescription(projectId);

        assertThat(reload().getDescriptionJa()).isNull();
    }

    @Test
    void OpenAI_키가_없으면_번역을_시도하지_않는다() {
        given(translationService.isAvailable()).willReturn(false);

        service.translateDescription(projectId);

        verify(translationService, never()).translateToJapanese(any(), any());
    }

    @Test
    void 소개가_없는_프로젝트는_번역하지_않는다() {
        Long emptyId = projectRepository.save(Project.builder().name("빈").teamName("팀").build()).getId();

        service.translateDescription(emptyId);

        verify(translationService, never()).translateToJapanese(any(), any());
    }

    static class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }
}
