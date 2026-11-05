package com.example.gifserverv2.global.security;

import com.example.gifserverv2.domain.engagement.controller.ProjectEngagementController;
import com.example.gifserverv2.domain.engagement.service.AnonymousViewService;
import com.example.gifserverv2.domain.engagement.service.ProjectEngagementService;
import com.example.gifserverv2.domain.project.controller.ProjectController;
import com.example.gifserverv2.domain.project.service.CommandProjectService;
import com.example.gifserverv2.domain.project.service.ProjectLinkService;
import com.example.gifserverv2.domain.project.service.ProjectNoteService;
import com.example.gifserverv2.domain.project.service.QueryProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ProjectEngagementController.class, ProjectController.class})
@Import({SecurityConfig.class, TokenAuthenticationFilter.class})
class SecurityConfigTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean JwtTokenProvider jwtTokenProvider;
    @MockitoBean ProjectEngagementService engagementService;
    @MockitoBean AnonymousViewService anonymousViewService;
    @MockitoBean QueryProjectService queryProjectService;
    @MockitoBean CommandProjectService commandProjectService;
    @MockitoBean ProjectNoteService projectNoteService;
    @MockitoBean ProjectLinkService projectLinkService;
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

    @Test
    void 비로그인_프로젝트_목록_조회는_허용된다() throws Exception {
        mockMvc.perform(get("/api/project/filter")).andExpect(status().isOk());
    }

    @Test
    void 비로그인_프로젝트_상세_조회는_허용된다() throws Exception {
        mockMvc.perform(get("/api/project/1")).andExpect(status().isOk());
    }

    @Test
    void 비로그인은_내_프로젝트_관리자_목록_통계_댓글_목록을_조회할_수_없다() throws Exception {
        // 열어준 경로(filter, 숫자 id)와 이름이 비슷한 경로는 여전히 인증이 필요해야 한다.
        mockMvc.perform(get("/api/project/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/project/admin")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/project/stats")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/project/1/comments")).andExpect(status().isUnauthorized());
    }

    @Test
    void 비로그인_프로젝트_수정은_401을_반환한다() throws Exception {
        mockMvc.perform(put("/api/project/1/update")).andExpect(status().isUnauthorized());
    }
}