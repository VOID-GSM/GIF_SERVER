package com.example.gifserverv2.domain.student.service;

import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.entity.ProjectMember;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.domain.push.service.PushSenderService;
import com.example.gifserverv2.domain.student.dto.response.RosterUploadResponse;
import com.example.gifserverv2.domain.student.dto.response.StudentResponse;
import com.example.gifserverv2.domain.student.dto.response.TeamJoinRequestResponse;
import com.example.gifserverv2.domain.student.dto.response.UnregisteredStudentResponse;
import com.example.gifserverv2.domain.student.entity.TeamJoinRequest.Status;
import com.example.gifserverv2.domain.student.repository.StudentRosterRepository;
import com.example.gifserverv2.domain.student.repository.TeamJoinRequestRepository;
import com.example.gifserverv2.domain.user.entity.AdminRole;
import com.example.gifserverv2.domain.user.entity.ClientRole;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Import({AdminStudentService.class, TeamJoinRequestService.class, StudentRosterService.class, StudentNotifier.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class StudentManagementTest {

    @Autowired AdminStudentService adminStudentService;
    @Autowired TeamJoinRequestService teamJoinRequestService;
    @Autowired StudentRosterService studentRosterService;
    @Autowired UserRepository userRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired ProjectMemberRepository projectMemberRepository;
    @Autowired TeamJoinRequestRepository teamJoinRequestRepository;
    @Autowired StudentRosterRepository studentRosterRepository;
    @MockitoBean PushSenderService pushSenderService;

    private AuthenticatedUser master;
    private UserEntity leader;
    private UserEntity member;
    private UserEntity loner;
    private Project projectA;
    private Project projectB;

    @BeforeEach
    void setUp() {
        UserEntity masterUser = new UserEntity("master@gsm.hs.kr", "마스터", null, Role.ADMIN);
        masterUser.updateAdminAdditionalInfo(AdminRole.MASTER, "마스터", null, false);
        masterUser = userRepository.save(masterUser);
        master = auth(masterUser, AdminRole.MASTER);

        leader = userRepository.save(new UserEntity("leader@gsm.hs.kr", "김팀장", "1101", Role.USER, "1"));
        member = userRepository.save(new UserEntity("member@gsm.hs.kr", "이팀원", "1102", Role.USER, "1"));
        loner = userRepository.save(new UserEntity("loner@gsm.hs.kr", "박누락", "1203", Role.USER, "1"));
        UserEntity leaderB = userRepository.save(new UserEntity("leaderb@gsm.hs.kr", "최팀장", "1104", Role.USER, "1"));

        projectA = projectRepository.save(Project.builder().name("A 프로젝트").teamName("A팀").build());
        projectB = projectRepository.save(Project.builder().name("B 프로젝트").teamName("B팀").build());
        addMember(projectA, leader, ClientRole.LEADER);
        addMember(projectA, member, ClientRole.MEMBER);
        addMember(projectB, leaderB, ClientRole.LEADER);
    }

    @AfterEach
    void tearDown() {
        teamJoinRequestRepository.deleteAll();
        projectMemberRepository.deleteAll();
        projectRepository.deleteAll();
        studentRosterRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void 학생을_이름_학번으로_검색하고_소속_팀을_함께_준다() {
        List<StudentResponse> byName = adminStudentService.getStudents("팀원", null, null);
        assertThat(byName).extracting(StudentResponse::name).containsExactly("이팀원");
        assertThat(byName.get(0).teams()).extracting(StudentResponse.TeamInfo::teamName, StudentResponse.TeamInfo::role)
                .containsExactly(tuple("A팀", ClientRole.MEMBER));

        assertThat(adminStudentService.getStudents("120", null, null))
                .extracting(StudentResponse::name).containsExactly("박누락");
    }

    @Test
    void 선생님은_학생_목록에_나오지_않는다() {
        assertThat(adminStudentService.getStudents(null, null, null))
                .extracting(StudentResponse::name).doesNotContain("마스터");
    }

    @Test
    void 미배정_학생만_조회한다() {
        assertThat(adminStudentService.getStudents(null, null, false))
                .extracting(StudentResponse::name).containsExactly("박누락");
    }

    @Test
    void 미배정_학생_배정은_팀장이_수락해야_팀원이_된다() {
        TeamJoinRequestResponse request = adminStudentService.requestAssign(master, loner.getId(), projectA.getId());

        assertThat(projectMemberRepository.existsByUserId(loner.getId())).isFalse();
        verify(pushSenderService).sendNotification(eq(leader.getId()), anyString(), anyString(), anyString(), anyString());
        assertThat(adminStudentService.getStudents("박누락", null, null).get(0).pendingRequest().projectId())
                .isEqualTo(projectA.getId());

        teamJoinRequestService.respond(auth(leader, null), request.requestId(), true, null);

        ProjectMember added = projectMemberRepository.findByProjectIdAndUserId(projectA.getId(), loner.getId()).orElseThrow();
        assertThat(added.getRole()).isEqualTo(ClientRole.MEMBER);
        assertThat(teamJoinRequestRepository.findById(request.requestId()).orElseThrow().getStatus()).isEqualTo(Status.ACCEPTED);
        verify(pushSenderService).sendNotification(eq(loner.getId()), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void 팀장이_거절하면_팀원이_되지_않고_요청한_관리자에게_알린다() {
        TeamJoinRequestResponse request = adminStudentService.requestAssign(master, loner.getId(), projectA.getId());

        teamJoinRequestService.respond(auth(leader, null), request.requestId(), false, "인원이 다 찼어요");

        assertThat(projectMemberRepository.existsByUserId(loner.getId())).isFalse();
        assertThat(teamJoinRequestRepository.findById(request.requestId()).orElseThrow().getRejectReason()).isEqualTo("인원이 다 찼어요");
        verify(pushSenderService).sendNotification(eq(master.userId()), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void 팀장이_아닌_팀원은_요청을_처리할_수_없다() {
        TeamJoinRequestResponse request = adminStudentService.requestAssign(master, loner.getId(), projectA.getId());

        assertThatThrownBy(() -> teamJoinRequestService.respond(auth(member, null), request.requestId(), true, null))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void 이미_팀이_있는_학생이나_대기_중인_요청이_있는_학생은_배정_요청할_수_없다() {
        assertStatus(() -> adminStudentService.requestAssign(master, member.getId(), projectB.getId()), HttpStatus.CONFLICT);

        adminStudentService.requestAssign(master, loner.getId(), projectA.getId());
        assertStatus(() -> adminStudentService.requestAssign(master, loner.getId(), projectB.getId()), HttpStatus.CONFLICT);
    }

    @Test
    void 대기_중에_다른_팀으로_배정되면_수락할_수_없다() {
        TeamJoinRequestResponse request = adminStudentService.requestAssign(master, loner.getId(), projectA.getId());
        addMember(projectB, loner, ClientRole.MEMBER);

        assertStatus(() -> teamJoinRequestService.respond(auth(leader, null), request.requestId(), true, null), HttpStatus.CONFLICT);
    }

    @Test
    void Master_Void가_아니면_관리할_수_없다() {
        UserEntity teacher = new UserEntity("t@gsm.hs.kr", "일반쌤", null, Role.ADMIN);
        teacher.updateAdminAdditionalInfo(AdminRole.GENERAL_TEACHER, "일반쌤", null, false);
        AuthenticatedUser general = auth(userRepository.save(teacher), AdminRole.GENERAL_TEACHER);

        assertStatus(() -> adminStudentService.requestAssign(general, loner.getId(), projectA.getId()), HttpStatus.FORBIDDEN);
        assertStatus(() -> adminStudentService.moveTeam(general, member.getId(), projectB.getId()), HttpStatus.FORBIDDEN);
        assertStatus(() -> studentRosterService.getUnregistered(general, null), HttpStatus.FORBIDDEN);
    }

    @Test
    void 팀원을_다른_팀으로_옮긴다() {
        adminStudentService.moveTeam(master, member.getId(), projectB.getId());

        assertThat(projectMemberRepository.findAllByUserId(member.getId()))
                .extracting(m -> m.getProject().getId(), ProjectMember::getRole)
                .containsExactly(tuple(projectB.getId(), ClientRole.MEMBER));
    }

    @Test
    void 팀장은_옮기거나_제외할_수_없다() {
        assertStatus(() -> adminStudentService.moveTeam(master, leader.getId(), projectB.getId()), HttpStatus.BAD_REQUEST);
        assertStatus(() -> adminStudentService.removeFromTeam(master, leader.getId(), projectA.getId()), HttpStatus.BAD_REQUEST);
    }

    @Test
    void 팀원을_팀에서_제외한다() {
        adminStudentService.removeFromTeam(master, member.getId(), projectA.getId());

        assertThat(projectMemberRepository.existsByUserId(member.getId())).isFalse();
    }

    @Test
    void 팀원을_팀장으로_바꾸면_기존_팀장은_팀원이_된다() {
        adminStudentService.changeRole(master, member.getId(), projectA.getId(), ClientRole.LEADER);

        assertThat(roleOf(projectA, member)).isEqualTo(ClientRole.LEADER);
        assertThat(roleOf(projectA, leader)).isEqualTo(ClientRole.MEMBER);
    }

    @Test
    void 팀장을_바로_팀원으로_바꿀_수는_없다() {
        assertStatus(() -> adminStudentService.changeRole(master, leader.getId(), projectA.getId(), ClientRole.MEMBER),
                HttpStatus.BAD_REQUEST);
        assertThat(roleOf(projectA, leader)).isEqualTo(ClientRole.LEADER);
    }

    @Test
    void 명단을_올리면_가입하지_않은_학생만_미가입으로_나온다() {
        String csv = "학번,이름\n1101,김팀장\n1301,정미가입\n2105,한미가입\n";

        RosterUploadResponse result = studentRosterService.upload(master, csv("roster.csv", csv, StandardCharsets.UTF_8));

        assertThat(result.created()).isEqualTo(3);
        assertThat(result.errors()).isEmpty();
        assertThat(studentRosterService.getUnregistered(master, null))
                .extracting(UnregisteredStudentResponse::studentNumber, UnregisteredStudentResponse::name)
                .containsExactly(tuple("1301", "정미가입"), tuple("2105", "한미가입"));
        assertThat(studentRosterService.getUnregistered(master, 2))
                .extracting(UnregisteredStudentResponse::name).containsExactly("한미가입");
    }

    @Test
    void 명단을_다시_올리면_이름을_갱신하고_잘못된_줄은_알려준다() {
        studentRosterService.upload(master, csv("a.csv", "1301,정미가입\n", StandardCharsets.UTF_8));

        RosterUploadResponse result = studentRosterService.upload(master,
                csv("b.csv", "1301,정바뀜\n9999,이상함\n1302,\n1303,중복\n1303,중복\n", StandardCharsets.UTF_8));

        assertThat(result.created()).isEqualTo(1);
        assertThat(result.updated()).isEqualTo(1);
        assertThat(result.errors()).extracting(RosterUploadResponse.RowError::line).containsExactly(2, 3, 5);
        assertThat(studentRosterService.getUnregistered(master, null))
                .extracting(UnregisteredStudentResponse::name).containsExactly("정바뀜", "중복");
    }

    @Test
    void 엑셀에서_저장한_CP949_명단도_읽는다() {
        RosterUploadResponse result = studentRosterService.upload(master,
                csv("excel.csv", "학번,이름\r\n1301,정미가입\r\n", Charset.forName("MS949")));

        assertThat(result.created()).isEqualTo(1);
        assertThat(studentRosterService.getUnregistered(master, null))
                .extracting(UnregisteredStudentResponse::name).containsExactly("정미가입");
    }

    @Test
    void 요청을_취소하면_팀장이_수락할_수_없다() {
        TeamJoinRequestResponse request = adminStudentService.requestAssign(master, loner.getId(), projectA.getId());
        adminStudentService.cancelRequest(master, request.requestId());

        assertStatus(() -> teamJoinRequestService.respond(auth(leader, null), request.requestId(), true, null), HttpStatus.CONFLICT);
        verify(pushSenderService, never()).sendNotification(eq(loner.getId()), anyString(), anyString(), anyString(), anyString());
    }

    private void addMember(Project project, UserEntity user, ClientRole role) {
        projectMemberRepository.save(ProjectMember.builder().project(project).userId(user.getId()).role(role).build());
    }

    private ClientRole roleOf(Project project, UserEntity user) {
        return projectMemberRepository.findByProjectIdAndUserId(project.getId(), user.getId()).orElseThrow().getRole();
    }

    private static AuthenticatedUser auth(UserEntity user, AdminRole adminRole) {
        return new AuthenticatedUser(user.getId(), user.getEmail(), user.getName(), user.getStudentNumber(),
                adminRole != null ? Role.ADMIN : Role.USER, adminRole, null, false, null);
    }

    private static MockMultipartFile csv(String filename, String content, Charset charset) {
        return new MockMultipartFile("file", filename, "text/csv", content.getBytes(charset));
    }

    private static void assertStatus(Runnable action, HttpStatus status) {
        assertThatThrownBy(action::run)
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(status);
    }
}
