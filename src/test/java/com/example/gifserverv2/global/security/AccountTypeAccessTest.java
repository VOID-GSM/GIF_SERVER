package com.example.gifserverv2.global.security;

import com.example.gifserverv2.domain.ai.service.AiSummaryService;
import com.example.gifserverv2.domain.assessment.catchase.controller.CatChaseController;
import com.example.gifserverv2.domain.assessment.catchase.service.CatChaseService;
import com.example.gifserverv2.domain.assessment.roadmaking.controller.RoadMakingController;
import com.example.gifserverv2.domain.assessment.roadmaking.service.RoadMakingService;
import com.example.gifserverv2.domain.engagement.controller.ProjectEngagementController;
import com.example.gifserverv2.domain.engagement.service.ProjectEngagementService;
import com.example.gifserverv2.domain.form.controller.FormController;
import com.example.gifserverv2.domain.form.service.AdminFormService;
import com.example.gifserverv2.domain.form.service.ClientFormService;
import com.example.gifserverv2.domain.form.service.FormFileService;
import com.example.gifserverv2.domain.coin.service.CoinSyncService;
import com.example.gifserverv2.domain.item.controller.ItemController;
import com.example.gifserverv2.domain.item.service.ItemService;
import com.example.gifserverv2.domain.project.controller.ProjectController;
import com.example.gifserverv2.domain.project.service.CommandProjectService;
import com.example.gifserverv2.domain.project.service.ProjectLinkService;
import com.example.gifserverv2.domain.project.service.ProjectNoteService;
import com.example.gifserverv2.domain.project.service.QueryProjectService;
import com.example.gifserverv2.domain.user.controller.MypageController;
import com.example.gifserverv2.domain.user.controller.UserController;
import com.example.gifserverv2.domain.user.controller.UserProfileController;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.domain.user.service.MypageService;
import com.example.gifserverv2.domain.user.service.UserService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.example.gifserverv2.domain.engagement.service.AnonymousViewService;

@WebMvcTest({
        ProjectController.class,
        ProjectEngagementController.class,
        FormController.class,
        MypageController.class,
        ItemController.class,
        UserController.class,
        UserProfileController.class,
        CatChaseController.class,
        RoadMakingController.class
})
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
    @MockitoBean ItemService itemService;
    @MockitoBean UserService userService;
    @MockitoBean CoinSyncService coinSyncService;
    @MockitoBean CatChaseService catChaseService;
    @MockitoBean RoadMakingService roadMakingService;

    @Test
    void 외부_사용자는_마이페이지를_사용할_수_있다() throws Exception {
        mockMvc.perform(get("/api/users/me/mypage").with(as(Role.EXTERNAL))).andExpect(status().isOk());
        mockMvc.perform(patch("/api/users/me/mypage").with(as(Role.EXTERNAL))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"major\":\"디자인\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(multipart(HttpMethod.PUT, "/api/users/me/profile-image")
                        .file("file", new byte[]{1}).with(as(Role.EXTERNAL)))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/users/me/profile-image").with(as(Role.EXTERNAL))).andExpect(status().isOk());
        mockMvc.perform(get("/api/users/me/links").with(as(Role.EXTERNAL))).andExpect(status().isOk());
        mockMvc.perform(post("/api/users/me/links").with(as(Role.EXTERNAL))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"https://blog.example.com\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(patch("/api/users/me/links/1").with(as(Role.EXTERNAL))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"https://blog.example.com\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/users/me/links/1").with(as(Role.EXTERNAL))).andExpect(status().isNoContent());
    }

    @Test
    void 로그인한_사용자는_누구나_다른_사용자_프로필을_조회할_수_있다() throws Exception {
        for (Role role : Role.values()) {
            mockMvc.perform(get("/api/users/2/profile").with(as(role))).andExpect(status().isOk());
        }
        mockMvc.perform(get("/api/users/2/profile")).andExpect(status().isUnauthorized());
    }

    @Test
    void 외부_사용자는_GitHub_연동을_사용할_수_없다() throws Exception {
        mockMvc.perform(post("/api/users/me/github/sync").with(as(Role.EXTERNAL)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(AccessDeniedCode.EXTERNAL_USER_FORBIDDEN.name()));
    }

    @Test
    void 외부_사용자는_Thyn_꾸미기를_사용할_수_있다() throws Exception {
        mockMvc.perform(get("/api/items").with(as(Role.EXTERNAL))).andExpect(status().isOk());
        mockMvc.perform(get("/api/items/category/HAIR").with(as(Role.EXTERNAL))).andExpect(status().isOk());
        mockMvc.perform(post("/api/items/buy").with(as(Role.EXTERNAL))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"itemId\":1}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/items/equip").with(as(Role.EXTERNAL))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"itemIds\":[]}"))
                .andExpect(status().isOk());
    }

    @Test
    void 외부_사용자는_역량검사를_사용할_수_있다() throws Exception {
        mockMvc.perform(post("/api/assessment/cat-chase/rounds").with(as(Role.EXTERNAL))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"questionCount\":3}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/assessment/cat-chase/rounds").with(as(Role.EXTERNAL))).andExpect(status().isOk());
        mockMvc.perform(get("/api/assessment/cat-chase/ranking").with(as(Role.EXTERNAL))).andExpect(status().isOk());
        mockMvc.perform(get("/api/assessment/road-making/ranking").with(as(Role.EXTERNAL))).andExpect(status().isOk());
    }

    @Test
    void 재학생은_역량검사를_사용할_수_있다() throws Exception {
        mockMvc.perform(get("/api/assessment/cat-chase/ranking").with(as(Role.USER))).andExpect(status().isOk());
        mockMvc.perform(get("/api/assessment/road-making/ranking").with(as(Role.USER))).andExpect(status().isOk());
    }

    @Test
    void 로그인하지_않으면_역량검사를_사용할_수_없다() throws Exception {
        mockMvc.perform(get("/api/assessment/cat-chase/ranking")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/assessment/road-making/ranking")).andExpect(status().isUnauthorized());
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