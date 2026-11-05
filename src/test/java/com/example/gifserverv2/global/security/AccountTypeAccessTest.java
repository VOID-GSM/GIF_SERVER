package com.example.gifserverv2.global.security;

import com.example.gifserverv2.domain.ai.service.AiSummaryService;
import com.example.gifserverv2.domain.engagement.controller.ProjectEngagementController;
import com.example.gifserverv2.domain.engagement.service.ProjectEngagementService;
import com.example.gifserverv2.domain.form.controller.FormController;
import com.example.gifserverv2.domain.form.service.AdminFormService;
import com.example.gifserverv2.domain.form.service.ClientFormService;
import com.example.gifserverv2.domain.form.service.FormFileService;
import com.example.gifserverv2.domain.project.controller.ProjectController;
import com.example.gifserverv2.domain.project.service.CommandProjectService;
import com.example.gifserverv2.domain.project.service.ProjectLinkService;
import com.example.gifserverv2.domain.project.service.ProjectNoteService;
import com.example.gifserverv2.domain.project.service.QueryProjectService;
import com.example.gifserverv2.domain.user.controller.MypageController;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.domain.user.service.MypageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.example.gifserverv2.domain.engagement.service.AnonymousViewService;

@WebMvcTest({ProjectController.class, ProjectEngagementController.class, FormController.class, MypageController.class})
@Import({SecurityConfig.class, TokenAuthenticationFilter.class})
class AccountTypeAccessTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean JwtTokenProvider jwtTokenProvider;
    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;
    @MockitoBean QueryProjectService queryProjectService;
    @MockitoBean AiSummaryService aiSummaryService;
    @MockitoBean AnonymousViewService anonymousViewService;
    @MockitoBean CommandProjectService commandProjectService;
    @MockitoBean ProjectNoteService projectNoteService;
    @MockitoBean ProjectLinkService projectLinkService;
    @MockitoBean ProjectEngagementService engagementService;
    @MockitoBean AdminFormService adminFormService;
    @MockitoBean ClientFormService clientFormService;
    @MockitoBean FormFileService formFileService;
    @MockitoBean MypageService mypageService;

    @Test
    void 외부_사용자는_마이페이지를_사용할_수_없다() throws Exception {
        mockMvc.perform(get("/api/users/me/mypage").with(as(Role.EXTERNAL)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(AccessDeniedCode.EXTERNAL_USER_FORBIDDEN.name()));
        mockMvc.perform(post("/api/users/me/links").with(as(Role.EXTERNAL))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"https://blog.example.com\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void 재학생은_마이페이지를_사용할_수_있다() throws Exception {
        mockMvc.perform(get("/api/users/me/mypage").with(as(Role.USER))).andExpect(status().isOk());
        mockMvc.perform(get("/api/users/me/links").with(as(Role.USER))).andExpect(status().isOk());
    }

    @Test
    void 외부_사용자는_프로젝트를_생성할_수_없다() throws Exception {
        mockMvc.perform(multipart("/api/project").with(as(Role.EXTERNAL)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(AccessDeniedCode.EXTERNAL_USER_FORBIDDEN.name()));
    }

    @Test
    void 외부_사용자는_프로젝트를_수정할_수_없다() throws Exception {
        mockMvc.perform(multipart(HttpMethod.PUT, "/api/project/1/update").with(as(Role.EXTERNAL)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(AccessDeniedCode.EXTERNAL_USER_FORBIDDEN.name()));
    }

    @Test
    void 외부_사용자는_양식을_제출할_수_없다() throws Exception {
        mockMvc.perform(post("/api/form/submit").with(as(Role.EXTERNAL))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(AccessDeniedCode.EXTERNAL_USER_FORBIDDEN.name()));
    }

    @Test
    void 외부_사용자는_내_프로젝트_목록을_조회할_수_없다() throws Exception {
        mockMvc.perform(get("/api/project/me").with(as(Role.EXTERNAL)))
                .andExpect(status().isForbidden());
    }

    @Test
    void 외부_사용자는_프로젝트를_조회할_수_있다() throws Exception {
        mockMvc.perform(get("/api/project/1").with(as(Role.EXTERNAL))).andExpect(status().isOk());
        mockMvc.perform(get("/api/project/filter").with(as(Role.EXTERNAL))).andExpect(status().isOk());
        mockMvc.perform(get("/api/project/1/comments").with(as(Role.EXTERNAL))).andExpect(status().isOk());
    }

    @Test
    void 외부_사용자는_좋아요와_댓글을_사용할_수_있다() throws Exception {
        mockMvc.perform(post("/api/project/1/like").with(as(Role.EXTERNAL))).andExpect(status().isOk());
        mockMvc.perform(delete("/api/project/1/like").with(as(Role.EXTERNAL))).andExpect(status().isOk());
        mockMvc.perform(post("/api/project/1/comments").with(as(Role.EXTERNAL))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"좋아요\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(delete("/api/project/1/comments/1").with(as(Role.EXTERNAL)))
                .andExpect(status().isNoContent());
    }

    @Test
    void 재학생은_팀_관리_API를_사용할_수_있다() throws Exception {
        mockMvc.perform(get("/api/project/me").with(as(Role.USER))).andExpect(status().isOk());
        mockMvc.perform(post("/api/form/submit").with(as(Role.USER))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/project/1/like").with(as(Role.USER))).andExpect(status().isOk());
    }

    @Test
    void 메서드_권한에서_막혀도_403_응답에_code가_포함된다() throws Exception {
        mockMvc.perform(get("/api/project/admin").with(as(Role.USER)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(AccessDeniedCode.ACCESS_DENIED.name()));
    }

    private RequestPostProcessor as(Role role) {
        AuthenticatedUser principal = new AuthenticatedUser(1L, "user@test.com", "사용자", null, role, null, null, false, null);
        return authentication(new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))));
    }
}
