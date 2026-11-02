package com.example.gifserverv2.global.security;

import com.example.gifserverv2.domain.engagement.controller.ProjectEngagementController;
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
    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void 비로그인_좋아요_요청은_401을_반환한다() throws Exception {
        mockMvc.perform(post("/api/project/1/like")).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/project/1/like")).andExpect(status().isUnauthorized());
    }
}
