package com.example.gifserverv2.domain.user.service;

import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.entity.ProjectMember;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.domain.user.dto.request.UpdateMypageRequest;
import com.example.gifserverv2.domain.user.dto.request.UserLinkRequest;
import com.example.gifserverv2.domain.user.dto.response.MypageResponse;
import com.example.gifserverv2.domain.user.dto.response.UserLinkResponse;
import com.example.gifserverv2.domain.user.entity.ClientRole;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserLinkRepository;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import com.example.gifserverv2.global.file.FileStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Import(MypageService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class MypageServiceTest {

    @Autowired MypageService service;
    @Autowired UserRepository userRepository;
    @Autowired UserLinkRepository linkRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired ProjectMemberRepository memberRepository;
    @MockitoBean FileStorageService fileStorageService;

    Long userId;

    @BeforeEach
    void setUp() {
        userId = userRepository.save(new UserEntity("s1203@gsm.hs.kr", "김학생", "1203", Role.USER, "1")).getId();
    }

    @AfterEach
    void tearDown() {
        linkRepository.deleteAll();
        memberRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void 기본_정보와_소속_팀을_조회한다() {
        Project member = projectRepository.save(Project.builder().name("VOID").teamName("보이드").build());
        Project leader = projectRepository.save(Project.builder().name("GIF").teamName("더모먼트").build());
        memberRepository.save(ProjectMember.builder().project(member).userId(userId).role(ClientRole.MEMBER).part("백엔드").build());
        memberRepository.save(ProjectMember.builder().project(leader).userId(userId).role(ClientRole.LEADER).part("프론트엔드").build());

        MypageResponse mypage = service.getMypage(userId);

        assertThat(mypage.name()).isEqualTo("김학생");
        assertThat(mypage.grade()).isEqualTo(1);
        assertThat(mypage.classNo()).isEqualTo(2);
        assertThat(mypage.profileImageUrl()).isNull();
        assertThat(mypage.customProfileImage()).isFalse();
        // 팀장인 팀이 먼저
        assertThat(mypage.teams())
                .extracting(MypageResponse.TeamInfo::teamName, MypageResponse.TeamInfo::role, MypageResponse.TeamInfo::part)
                .containsExactly(tuple("더모먼트", "LEADER", "프론트엔드"), tuple("보이드", "MEMBER", "백엔드"));
    }

    @Test
    void 전공과_기술_스택을_수정한다() {
        MypageResponse updated = service.updateMypage(userId,
                new UpdateMypageRequest("  백엔드  ", List.of(" Spring ", "React", "spring", "MySQL")));

        assertThat(updated.major()).isEqualTo("백엔드");
        assertThat(updated.techStacks()).containsExactly("Spring", "React", "MySQL");
        assertThat(service.getMypage(userId).techStacks()).containsExactly("Spring", "React", "MySQL");
    }

    @Test
    void null_필드는_유지하고_빈_값은_삭제한다() {
        service.updateMypage(userId, new UpdateMypageRequest("백엔드", List.of("Spring")));

        MypageResponse kept = service.updateMypage(userId, new UpdateMypageRequest(null, null));
        assertThat(kept.major()).isEqualTo("백엔드");
        assertThat(kept.techStacks()).containsExactly("Spring");

        MypageResponse cleared = service.updateMypage(userId, new UpdateMypageRequest("", List.of()));
        assertThat(cleared.major()).isNull();
        assertThat(cleared.techStacks()).isEmpty();
    }

    @Test
    void 프로필_이미지를_올리면_기존_이미지를_지우고_삭제하면_GitHub_아바타로_돌아간다() {
        UserEntity user = userRepository.findById(userId).orElseThrow();
        user.connectGithub("octocat", "https://avatars/octocat", "token", 0);
        userRepository.save(user);
        given(fileStorageService.save(any(), eq("users/profile")))
                .willReturn("https://files/users/profile/a.png", "https://files/users/profile/b.png");

        assertThat(service.getMypage(userId).profileImageUrl()).isEqualTo("https://avatars/octocat");

        service.updateProfileImage(userId, image("a.png"));
        MypageResponse replaced = service.updateProfileImage(userId, image("b.png"));
        assertThat(replaced.profileImageUrl()).isEqualTo("https://files/users/profile/b.png");
        assertThat(replaced.customProfileImage()).isTrue();
        verify(fileStorageService).delete("https://files/users/profile/a.png");

        MypageResponse deleted = service.deleteProfileImage(userId);
        assertThat(deleted.profileImageUrl()).isEqualTo("https://avatars/octocat");
        assertThat(deleted.customProfileImage()).isFalse();
        verify(fileStorageService).delete("https://files/users/profile/b.png");
    }

    @ParameterizedTest
    @ValueSource(strings = {"evil.svg", "doc.pdf", "noext"})
    void 이미지가_아닌_파일은_저장하지_않고_400(String filename) {
        assertStatus(HttpStatus.BAD_REQUEST, () -> service.updateProfileImage(userId, image(filename)));
        verify(fileStorageService, never()).save(any(), any());
    }

    @Test
    void 개인_링크를_추가_수정_삭제한다() {
        UserLinkResponse blog = service.addLink(userId, new UserLinkRequest(" 블로그 ", " https://blog.example.com "));
        UserLinkResponse github = service.addLink(userId, new UserLinkRequest(null, "https://github.com/octocat"));

        service.updateLink(userId, blog.id(), new UserLinkRequest("기술 블로그", "https://velog.io/@me"));
        service.deleteLink(userId, github.id());

        assertThat(service.getLinks(userId))
                .extracting(UserLinkResponse::title, UserLinkResponse::url)
                .containsExactly(tuple("기술 블로그", "https://velog.io/@me"));
        assertThat(service.getMypage(userId).links()).hasSize(1);
    }

    @Test
    void 개인_링크는_최대_5개까지_등록할_수_있다() {
        for (int i = 0; i < 5; i++) {
            service.addLink(userId, new UserLinkRequest(null, "https://site" + i + ".example.com"));
        }

        assertStatus(HttpStatus.CONFLICT,
                () -> service.addLink(userId, new UserLinkRequest(null, "https://site5.example.com")));
        assertThat(service.getLinks(userId)).hasSize(5);
    }

    @ParameterizedTest
    @ValueSource(strings = {"javascript:alert(1)", "ftp://files.example.com", "blog.example.com", "https://", "http://exa mple.com"})
    void http_https가_아닌_링크는_400(String url) {
        assertStatus(HttpStatus.BAD_REQUEST, () -> service.addLink(userId, new UserLinkRequest(null, url)));
    }

    @Test
    void 다른_사람의_링크는_수정_삭제할_수_없다() {
        Long otherId = userRepository.save(new UserEntity("s1204@gsm.hs.kr", "이학생", "1204", Role.USER, "1")).getId();
        UserLinkResponse others = service.addLink(otherId, new UserLinkRequest(null, "https://other.example.com"));

        assertStatus(HttpStatus.NOT_FOUND,
                () -> service.updateLink(userId, others.id(), new UserLinkRequest(null, "https://mine.example.com")));
        assertStatus(HttpStatus.NOT_FOUND, () -> service.deleteLink(userId, others.id()));
        assertThat(service.getLinks(otherId)).extracting(UserLinkResponse::url).containsExactly("https://other.example.com");
    }

    private static MockMultipartFile image(String filename) {
        return new MockMultipartFile("file", filename, "image/png", new byte[]{1, 2, 3});
    }

    private static void assertStatus(HttpStatus status, Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(status));
    }
}
