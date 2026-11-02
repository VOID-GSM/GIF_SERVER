package com.example.gifserverv2.domain.auth.service;

import com.example.gifserverv2.domain.auth.dto.response.OAuthSignInResponse;
import com.example.gifserverv2.domain.user.entity.AdminRole;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import com.example.gifserverv2.global.config.OAuthProperties;
import com.example.gifserverv2.global.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import team.themoment.datagsm.sdk.oauth.DataGsmOAuthClient;
import team.themoment.datagsm.sdk.oauth.model.AccountObjectType;
import team.themoment.datagsm.sdk.oauth.model.AccountStatus;
import team.themoment.datagsm.sdk.oauth.model.Student;
import team.themoment.datagsm.sdk.oauth.model.Teacher;
import team.themoment.datagsm.sdk.oauth.model.TokenResponse;
import team.themoment.datagsm.sdk.oauth.model.UserInfo;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    private static final String REDIRECT_URI = "https://admin.example.com/callback";
    private static final String TEACHER_EMAIL = "teacher@gsm.hs.kr";

    @Mock
    private DataGsmOAuthClient dataGsmOAuthClient;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        OAuthProperties oauthProperties = new OAuthProperties();
        oauthProperties.getDatagsm().setRedirectUris(List.of(REDIRECT_URI));
        authService = new AuthService(dataGsmOAuthClient, userRepository, jwtTokenProvider, oauthProperties);

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken("dg-token");
        when(dataGsmOAuthClient.exchangeCodeForToken("code", REDIRECT_URI, "verifier")).thenReturn(tokenResponse);
        when(jwtTokenProvider.createToken(any())).thenReturn("jwt");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void 선생님은_teacher_이름과_ADMIN_권한으로_가입된다() {
        givenUserInfo(teacherInfo("김선생"));
        when(userRepository.findByEmail(TEACHER_EMAIL)).thenReturn(Optional.empty());

        OAuthSignInResponse response = signIn();

        assertThat(response.name()).isEqualTo("김선생");
        assertThat(response.role()).isEqualTo(Role.ADMIN.name());
        assertThat(response.studentNumber()).isNull();
    }

    @Test
    void 기존_Google_관리자는_이메일로_연결되고_관리자_부가정보가_유지된다() {
        UserEntity existing = new UserEntity(TEACHER_EMAIL, "구글이름", null, Role.ADMIN);
        existing.updateAdminAdditionalInfo(AdminRole.MAJOR_TEACHER, "구글이름", "1팀", true);
        givenUserInfo(teacherInfo("김선생"));
        when(userRepository.findByEmail(TEACHER_EMAIL)).thenReturn(Optional.of(existing));

        signIn();

        verify(userRepository, never()).save(any());
        assertThat(existing.getName()).isEqualTo("김선생");
        assertThat(existing.getRole()).isEqualTo(Role.ADMIN);
        assertThat(existing.getAdminRole()).isEqualTo(AdminRole.MAJOR_TEACHER);
        assertThat(existing.getAdminTeam()).isEqualTo("1팀");
        assertThat(existing.isGradeHead()).isTrue();
    }

    @Test
    void 학생은_USER_권한으로_가입된다() {
        givenUserInfo(studentInfo(2, 2101));
        when(userRepository.findByEmail("student@gsm.hs.kr")).thenReturn(Optional.empty());

        OAuthSignInResponse response = signIn();

        assertThat(response.name()).isEqualTo("김학생");
        assertThat(response.role()).isEqualTo(Role.USER.name());
        assertThat(response.studentNumber()).isEqualTo("2101");
    }

    @Test
    void teacher_객체가_없으면_400() {
        UserInfo userInfo = teacherInfo("김선생");
        userInfo.setTeacher(null);
        givenUserInfo(userInfo);

        assertSignInFails(HttpStatus.BAD_REQUEST);
    }

    @Test
    void 선생님_이름이_비어_있으면_400() {
        givenUserInfo(teacherInfo(" "));

        assertSignInFails(HttpStatus.BAD_REQUEST);
    }

    @Test
    void 승인_대기_계정은_403() {
        UserInfo userInfo = teacherInfo("김선생");
        userInfo.setStatus(AccountStatus.PENDING);
        givenUserInfo(userInfo);

        assertSignInFails(HttpStatus.FORBIDDEN);
    }

    @Test
    void 상태를_알_수_없는_계정은_403() {
        UserInfo userInfo = teacherInfo("김선생");
        userInfo.setStatus(null);
        givenUserInfo(userInfo);

        assertSignInFails(HttpStatus.FORBIDDEN);
    }

    @Test
    void 계정_유형이_없으면_관리자가_되지_않고_403() {
        UserInfo userInfo = new UserInfo();
        userInfo.setEmail("unknown@gsm.hs.kr");
        userInfo.setStatus(AccountStatus.ACTIVE);
        userInfo.setIsStudent(false);
        givenUserInfo(userInfo);

        assertSignInFails(HttpStatus.FORBIDDEN);
    }

    @Test
    void 삼학년_학생은_기존처럼_403() {
        givenUserInfo(studentInfo(3, 3101));

        assertSignInFails(HttpStatus.FORBIDDEN);
    }

    private OAuthSignInResponse signIn() {
        return authService.signIn("code", REDIRECT_URI, "verifier");
    }

    private void assertSignInFails(HttpStatus status) {
        assertThatThrownBy(this::signIn)
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(status));
        verify(jwtTokenProvider, never()).createToken(any());
        verify(userRepository, never()).save(any());
    }

    private void givenUserInfo(UserInfo userInfo) {
        when(dataGsmOAuthClient.getUserInfo("dg-token")).thenReturn(userInfo);
    }

    private UserInfo teacherInfo(String name) {
        Teacher teacher = new Teacher();
        teacher.setName(name);
        teacher.setEmail(TEACHER_EMAIL);

        UserInfo userInfo = new UserInfo();
        userInfo.setEmail(TEACHER_EMAIL);
        userInfo.setStatus(AccountStatus.ACTIVE);
        userInfo.setObjectType(AccountObjectType.TEACHER);
        userInfo.setIsStudent(false);
        userInfo.setTeacher(teacher);
        return userInfo;
    }

    private UserInfo studentInfo(int grade, int studentNumber) {
        Student student = new Student();
        student.setName("김학생");
        student.setGrade(grade);
        student.setStudentNumber(studentNumber);

        UserInfo userInfo = new UserInfo();
        userInfo.setEmail("student@gsm.hs.kr");
        userInfo.setStatus(AccountStatus.ACTIVE);
        userInfo.setObjectType(AccountObjectType.STUDENT);
        userInfo.setIsStudent(true);
        userInfo.setStudent(student);
        return userInfo;
    }
}
