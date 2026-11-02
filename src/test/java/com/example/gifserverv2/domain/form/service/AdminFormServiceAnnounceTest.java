package com.example.gifserverv2.domain.form.service;

import com.example.gifserverv2.domain.form.entity.Form;
import com.example.gifserverv2.domain.push.service.PushSenderService;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminFormServiceAnnounceTest {

    @Mock QueryFormService queryFormService;
    @Mock UserRepository userRepository;
    @Mock PushSenderService pushSenderService;
    @InjectMocks AdminFormService adminFormService;

    @Test
    void 양식_등록_알림에_양식_제목과_이동_경로가_들어간다() {
        Form form = Form.builder()
                .id(7L)
                .title("1학기 중간 회고")
                .deadline(LocalDateTime.now().plusDays(3))
                .createdAt(LocalDateTime.now())
                .build();
        given(queryFormService.getFormOrThrow(7L)).willReturn(form);
        given(userRepository.findAllStudentIds()).willReturn(List.of(1L, 2L));

        adminFormService.announceForm(7L);

        verify(pushSenderService).sendBulkNotifications(
                List.of(1L, 2L),
                "📢 새로운 양식이 등록되었습니다.",
                "'1학기 중간 회고' 양식이 등록되었습니다. 기간 내에 작성해 주세요.",
                "/form/7/submit"
        );
    }
}
