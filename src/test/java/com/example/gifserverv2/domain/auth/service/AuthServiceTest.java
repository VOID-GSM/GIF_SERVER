package com.example.gifserverv2.domain.auth.service;

import com.example.gifserverv2.domain.auth.dto.response.OAuthSignInResponse;
import com.example.gifserverv2.domain.user.entity.AccountType;
import com.example.gifserverv2.domain.user.entity.AdminRole;
import com.example.gifserverv2.domain.user.entity.ClientRole;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import team.themoment.datagsm.sdk.oauth.DataGsmOAuthClient;
import team.themoment.datagsm.sdk.oauth.model.AccountObjectType;
import team.themoment.datagsm.sdk.oauth.model.AccountStatus;
import team.themoment.datagsm.sdk.oauth.model.Student;
import team.themoment.datagsm.sdk.oauth.model.Teacher;
import team.themoment.datagsm.sdk.oauth.model.TokenResponse;
import team.themoment.datagsm.sdk.oauth.model.UserInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        assertThat(response.accountType()).isEqualTo(AccountType.TEACHER.name());
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
    void 관리자_테스트_이메일의_학생_계정은_선생님으로_가입된다() {
        ReflectionTestUtils.setField(authService, "adminEmails", List.of(" Student@GSM.hs.kr "));
        // 3학년이어도 차단하지 않는다
        givenUserInfo(studentInfo(3, 3101));
        when(userRepository.findByEmail("student@gsm.hs.kr")).thenReturn(Optional.empty());

        OAuthSignInResponse response = signIn();

        assertThat(response.name()).isEqualTo("김학생");
        assertThat(response.role()).isEqualTo(Role.ADMIN.name());
        assertThat(response.accountType()).isEqualTo(AccountType.TEACHER.name());
        assertThat(response.studentNumber()).isNull();
        assertThat(response.grade()).isNull();
        assertThat(response.adminRole()).isNull();
    }

    @Test
    void 기존_학생_계정을_관리자_테스트_이메일로_등록하면_선생님으로_전환된다() {
        ReflectionTestUtils.setField(authService, "adminEmails", List.of("student@gsm.hs.kr"));
        UserEntity existing = new UserEntity("student@gsm.hs.kr", "김학생", "2101", Role.USER, "2");
        existing.updateClientAdditionalInfo(ClientRole.LEADER);
        givenUserInfo(studentInfo(2, 2101));
        when(userRepository.findByEmail("student@gsm.hs.kr")).thenReturn(Optional.of(existing));

        OAuthSignInResponse response = signIn();

        assertThat(response.role()).isEqualTo(Role.ADMIN.name());
        assertThat(existing.getEffectiveRole()).isEqualTo(Role.ADMIN);
        assertThat(existing.getClientRole()).isNull();
        assertThat(existing.getStudentNumber()).isNull();
    }

    @Test
    void 관리자_테스트_이메일이_아니면_학생은_그대로_USER다() {
        ReflectionTestUtils.setField(authService, "adminEmails", List.of("someone-else@gsm.hs.kr"));
        givenUserInfo(studentInfo(2, 2101));
        when(userRepository.findByEmail("student@gsm.hs.kr")).thenReturn(Optional.empty());

        assertThat(signIn().role()).isEqualTo(Role.USER.name());
    }

    @Test
    void 학생은_USER_권한으로_가입된다() {
        givenUserInfo(studentInfo(2, 2101));
        when(userRepository.findByEmail("student@gsm.hs.kr")).thenReturn(Optional.empty());

        OAuthSignInResponse response = signIn();

        assertThat(response.name()).isEqualTo("김학생");
        assertThat(response.role()).isEqualTo(Role.USER.name());
        assertThat(response.accountType()).isEqualTo(AccountType.STUDENT.name());
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

    @Test
    void Google_신규_로그인은_EXTERNAL_권한으로_가입된다() {
        when(userRepository.findByEmail("visitor@gmail.com")).thenReturn(Optional.empty());

        OAuthSignInResponse response = authService.signInWithGoogle("google-token", googleInfo("visitor@gmail.com", "방문객"));

        assertThat(response.role()).isEqualTo(Role.EXTERNAL.name());
        assertThat(response.accountType()).isEqualTo(AccountType.EXTERNAL.name());
        assertThat(response.name()).isEqualTo("방문객");
        assertThat(response.adminRole()).isNull();
        assertThat(response.studentNumber()).isNull();
    }

    @Test
    void Google_이름이_없으면_이메일_앞부분을_이름으로_쓴다() {
        when(userRepository.findByEmail("visitor@gmail.com")).thenReturn(Optional.empty());

        OAuthSignInResponse response = authService.signInWithGoogle("google-token", googleInfo("visitor@gmail.com", null));

        assertThat(response.name()).isEqualTo("visitor");
    }

    @Test
    void Google_재로그인은_기존_외부_사용자를_재사용하고_수정한_이름을_유지한다() {
        UserEntity existing = new UserEntity("visitor@gmail.com", "직접바꾼이름", null, Role.EXTERNAL);
        when(userRepository.findByEmail("visitor@gmail.com")).thenReturn(Optional.of(existing));

        OAuthSignInResponse response = authService.signInWithGoogle("google-token", googleInfo("visitor@gmail.com", "구글이름"));

        verify(userRepository, never()).save(any());
        assertThat(response.name()).isEqualTo("직접바꾼이름");
        assertThat(existing.getRole()).isEqualTo(Role.EXTERNAL);
    }

    @Test
    void Google_로그인은_관리자_계정을_변경하지_않고_403() {
        UserEntity admin = new UserEntity(TEACHER_EMAIL, "김선생", null, Role.ADMIN);
        admin.updateAdminAdditionalInfo(AdminRole.MASTER, "김선생", "1팀", true);
        when(userRepository.findByEmail(TEACHER_EMAIL)).thenReturn(Optional.of(admin));

        assertGoogleSignInForbidden(TEACHER_EMAIL);

        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(admin.getAdminRole()).isEqualTo(AdminRole.MASTER);
        assertThat(admin.getName()).isEqualTo("김선생");
    }

    @Test
    void Google_로그인은_학생_계정을_변경하지_않고_403() {
        UserEntity student = new UserEntity("student@gsm.hs.kr", "김학생", "2101", Role.USER, "2");
        when(userRepository.findByEmail("student@gsm.hs.kr")).thenReturn(Optional.of(student));

        assertGoogleSignInForbidden("student@gsm.hs.kr");

        assertThat(student.getRole()).isEqualTo(Role.USER);
        assertThat(student.getStudentNumber()).isEqualTo("2101");
    }

    @Test
    void VOID_이메일도_Google_로그인으로는_관리자가_되지_않는다() {
        ReflectionTestUtils.setField(authService, "voidEmails", List.of("void@gmail.com"));
        when(userRepository.findByEmail("void@gmail.com")).thenReturn(Optional.empty());

        OAuthSignInResponse response = authService.signInWithGoogle("google-token", googleInfo("void@gmail.com", "보이드"));

        assertThat(response.role()).isEqualTo(Role.EXTERNAL.name());
        assertThat(response.adminRole()).isNull();
    }

    @Test
    void 외부_사용자가_DataGSM으로_로그인하면_교내_권한으로_전환된다() {
        UserEntity external = new UserEntity(TEACHER_EMAIL, "구글이름", null, Role.EXTERNAL);
        givenUserInfo(teacherInfo("김선생"));
        when(userRepository.findByEmail(TEACHER_EMAIL)).thenReturn(Optional.of(external));

        OAuthSignInResponse response = signIn();

        assertThat(response.role()).isEqualTo(Role.ADMIN.name());
    }

    private void assertGoogleSignInForbidden(String email) {
        assertThatThrownBy(() -> authService.signInWithGoogle("google-token", googleInfo(email, "구글이름")))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
        verify(jwtTokenProvider, never()).createToken(any());
    }

    private Map<String, Object> googleInfo(String email, String name) {
        Map<String, Object> info = new HashMap<>();
        info.put("email", email);
        info.put("name", name);
        return info;
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
