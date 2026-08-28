package com.example.gifserverv2.domain.form.controller;

import com.example.gifserverv2.domain.ai.service.AiSummaryService;
import com.example.gifserverv2.domain.form.service.AdminFormService;
import com.example.gifserverv2.domain.form.service.ClientFormService;
import com.example.gifserverv2.domain.form.service.FormFileService;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.global.exception.GlobalExceptionHandler;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FormUpdateRequestBodyTest {

    private MockMvc mockMvc;
    private AdminFormService adminFormService;

    @BeforeEach
    void setUp() {
        adminFormService = mock(AdminFormService.class);
        FormController controller = new FormController(
                adminFormService,
                mock(ClientFormService.class),
                mock(FormFileService.class),
                mock(AiSummaryService.class)
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void loginAsAdmin() {
        AuthenticatedUser principal = new AuthenticatedUser(1L, "admin@test.com", "관리자", null, Role.ADMIN, null, null, false, null);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    @Test
    void body_없이_보내면_500이_아니라_400() throws Exception {
        mockMvc.perform(patch("/api/form/update")
                        .param("formId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(adminFormService);
    }

    @Test
    void deadline_포맷이_잘못되면_500이_아니라_400() throws Exception {
        String malformedBody = """
                {
                  "title": "테스트",
                  "description": "설명",
                  "deadline": "2026-08-30",
                  "targetGrade": 2,
                  "fields": []
                }
                """;

        mockMvc.perform(patch("/api/form/update")
                        .param("formId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(adminFormService);
    }

    @Test
    void 정상_body는_204로_처리된다() throws Exception {
        loginAsAdmin();
        String validBody = """
                {
                  "title": "테스트",
                  "description": "설명",
                  "deadline": "2026-09-15T23:59:59",
                  "targetGrade": 2,
                  "fields": []
                }
                """;

        mockMvc.perform(patch("/api/form/update")
                        .param("formId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody))
                .andExpect(status().isNoContent());
    }
}
