package com.example.gifserverv2.global.security;

import com.example.gifserverv2.domain.engagement.controller.ProjectEngagementController;
import com.example.gifserverv2.domain.engagement.service.AnonymousViewService;
import com.example.gifserverv2.domain.engagement.service.ProjectEngagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectEngagementController.class)
@Import({SecurityConfig.class, TokenAuthenticationFilter.class})
class SecurityConfigTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean JwtTokenProvider jwtTokenProvider;
    @MockitoBean ProjectEngagementService engagementService;
    @MockitoBean AnonymousViewService anonymousViewService;
    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void 비로그인_좋아요_요청은_401을_반환한다() throws Exception {
        mockMvc.perform(post("/api/project/1/like")).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/project/1/like")).andExpect(status().isUnauthorized());
    }

    @Test
    void 비로그인_조회수_요청은_허용된다() throws Exception {
        // 조회수 경로만 permitAll 이라 토큰 없이 호출해도 401이 아니라 200이어야 한다. (서비스는 가짜)
        mockMvc.perform(post("/api/project/1/view")).andExpect(status().isOk());
    }
}