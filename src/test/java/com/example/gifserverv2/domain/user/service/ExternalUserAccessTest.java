package com.example.gifserverv2.domain.user.service;

import com.example.gifserverv2.domain.auth.dto.request.UpdateCurrentUserRequest;
import com.example.gifserverv2.domain.auth.service.GithubOAuthClient;
import com.example.gifserverv2.domain.coin.service.GithubFetchService;
import com.example.gifserverv2.domain.notice.controller.NoticeController;
import com.example.gifserverv2.domain.notice.service.NoticeService;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import com.example.gifserverv2.domain.user.entity.ClientRole;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import com.example.gifserverv2.global.security.JwtTokenProvider;
import com.example.gifserverv2.global.security.TokenAuthenticationFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(ExternalUserAccessTest.MethodSecurityConfig.class)
class ExternalUserAccessTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-test-secret";

    @Configuration
    @EnableMethodSecurity
    static class MethodSecurityConfig {
        @Bean
        NoticeService noticeService() {
            return Mockito.mock(NoticeService.class);
        }

        @Bean
        NoticeController noticeController(NoticeService noticeService) {
            return new NoticeController(noticeService);
        }
    }

    @Autowired
    private NoticeController noticeController;

    private UserRepository userRepository;
    private UserService userService;
    private JwtTokenProvider jwtTokenProvider;
    private UserEntity external;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        jwtTokenProvider = new JwtTokenProvider(SECRET, 60);
        userService = new UserService(userRepository, mock(GithubOAuthClient.class), mock(GithubFetchService.class),
                mock(ProjectMemberRepository.class), jwtTokenProvider);

        external = new UserEntity("visitor@gmail.com", "방문객", null, Role.EXTERNAL);
        ReflectionTestUtils.setField(external, "id", 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(external));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @WithMockUser(roles = "EXTERNAL")
    void 외부_사용자는_관리자_API에_접근할_수_없다() {
        assertThatThrownBy(() -> noticeController.deleteNotice(1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void 관리자는_관리자_API에_접근할_수_있다() {
        assertThat(noticeController.deleteNotice(1L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void 발급된_토큰은_ROLE_EXTERNAL로_인증된다() throws Exception {
        SecurityContextHolder.clearContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + jwtTokenProvider.createToken(external));

        new TokenAuthenticationFilter(jwtTokenProvider)
                .doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_EXTERNAL");
    }

    @Test
    void 외부_사용자는_clientRole을_설정해_권한을_올릴_수_없다() {
        UpdateCurrentUserRequest request = new UpdateCurrentUserRequest(null, null, null, null, ClientRole.LEADER);

        assertForbidden(request);

        assertThat(external.getClientRole()).isNull();
        assertThat(external.getEffectiveRole()).isEqualTo(Role.EXTERNAL);
    }

    @Test
    void 외부_사용자는_학번을_설정할_수_없다() {
        UpdateCurrentUserRequest request = new UpdateCurrentUserRequest(null, "2101", null, null, null);

        assertForbidden(request);

        assertThat(external.getStudentNumber()).isNull();
    }

    private void assertForbidden(UpdateCurrentUserRequest request) {
        assertThatThrownBy(() -> userService.updateCurrentUser(caller(), request))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    private AuthenticatedUser caller() {
        return new AuthenticatedUser(1L, external.getEmail(), external.getName(), null, Role.EXTERNAL, null, null, false, null);
    }
}
