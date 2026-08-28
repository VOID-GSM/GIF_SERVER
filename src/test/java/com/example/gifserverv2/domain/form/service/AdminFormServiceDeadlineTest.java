package com.example.gifserverv2.domain.form.service;

import com.example.gifserverv2.domain.form.dto.request.CreateFormRequest;
import com.example.gifserverv2.domain.form.dto.request.UpdateFormRequest;
import com.example.gifserverv2.domain.form.exception.FormException;
import com.example.gifserverv2.domain.form.repository.FormRepository;
import com.example.gifserverv2.domain.form.repository.FormSubmitRepository;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.domain.push.service.PushSenderService;
import com.example.gifserverv2.domain.user.entity.AdminRole;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import com.example.gifserverv2.global.file.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminFormServiceDeadlineTest {

    @Mock private FormRepository formRepository;
    @Mock private FormSubmitRepository formSubmitRepository;
    @Mock private QueryFormService queryFormService;
    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;
    @Mock private PushSenderService pushSenderService;
    @Mock private FileStorageService fileStorageService;

    @InjectMocks
    private AdminFormService adminFormService;

    private UserEntity adminUser;

    @BeforeEach
    void setUp() {
        adminUser = new UserEntity("admin@test.com", "관리자", null, Role.ADMIN);
        adminUser.updateAdminAdditionalInfo(AdminRole.MASTER, "관리자", null, false);
    }

    @Test
    void 양식_생성시_과거_마감일이면_예외() {
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(adminUser));

        CreateFormRequest request = new CreateFormRequest(
                "테스트 양식",
                LocalDateTime.now().minusDays(1),
                java.util.List.of()
        );

        assertThatThrownBy(() -> adminFormService.createForm(1L, request))
                .isInstanceOf(FormException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(400))
                .hasMessageContaining("마감일");

        verify(formRepository, never()).save(any());
    }

    @Test
    void 양식_생성시_미래_마감일이면_정상_저장() {
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(adminUser));
        when(formRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateFormRequest request = new CreateFormRequest(
                "테스트 양식",
                LocalDateTime.now().plusDays(1),
                java.util.List.of()
        );

        adminFormService.createForm(1L, request);

        verify(formRepository, times(1)).save(any());
    }

    @Test
    void 양식_수정시_과거_마감일이면_예외() {
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(adminUser));

        UpdateFormRequest request = new UpdateFormRequest(
                "제목", "설명",
                LocalDateTime.now().minusHours(1),
                null,
                java.util.List.of()
        );

        assertThatThrownBy(() -> adminFormService.updateForm(1L, 10L, request))
                .isInstanceOf(FormException.class)
                .hasMessageContaining("마감일");

        verify(queryFormService, never()).getFormOrThrow(any());
    }
}
