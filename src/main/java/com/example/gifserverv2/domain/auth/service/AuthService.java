package com.example.gifserverv2.domain.auth.service;

import com.example.gifserverv2.domain.auth.dto.request.OAuthSignInRequest;
import com.example.gifserverv2.domain.auth.dto.response.OAuthSignInResponse;
import com.example.gifserverv2.domain.auth.dto.response.CurrentUserResponse;
import com.example.gifserverv2.domain.auth.dto.request.UpdateCurrentUserRequest;
import com.example.gifserverv2.domain.user.entity.ClientRole;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import com.example.gifserverv2.domain.user.entity.AdminRole;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import com.example.gifserverv2.domain.project.entity.ProjectMember;
import com.example.gifserverv2.global.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.gifserverv2.global.config.OAuthProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import team.themoment.datagsm.sdk.oauth.DataGsmOAuthClient;
import team.themoment.datagsm.sdk.oauth.exception.DataGsmException;
import team.themoment.datagsm.sdk.oauth.model.Student;
import team.themoment.datagsm.sdk.oauth.model.TokenResponse;
import team.themoment.datagsm.sdk.oauth.model.UserInfo;

import java.util.Map;
import java.util.List;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final DataGsmOAuthClient dataGsmOAuthClient;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final OAuthProperties oauthProperties;
    private final ProjectMemberRepository projectMemberRepository;

    public AuthService(DataGsmOAuthClient dataGsmOAuthClient, UserRepository userRepository,
                       JwtTokenProvider jwtTokenProvider, OAuthProperties oauthProperties, ProjectMemberRepository projectMemberRepository) {
        this.dataGsmOAuthClient = dataGsmOAuthClient;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.oauthProperties = oauthProperties;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional
    public OAuthSignInResponse signIn(OAuthSignInRequest request) {
        return signIn(request.authCode(), request.redirectUri(), request.codeVerifier());
    }

    @Transactional
    public OAuthSignInResponse signIn(String authCode, String redirectUri, String codeVerifier) {
        assertAllowedRedirectUri(redirectUri);

        try {
            TokenResponse tokenResponse = dataGsmOAuthClient.exchangeCodeForToken(
                    authCode,
                    redirectUri,
                    codeVerifier);
            UserInfo userInfo = dataGsmOAuthClient.getUserInfo(tokenResponse.getAccessToken());

            Student student = userInfo.getStudent();
            String email = userInfo.getEmail();
            boolean isStudent = userInfo.isStudent();

            String name = null;
            String studentNumber = null;
            String grade = null;
            Role assignedRole = Role.USER;

            if (!isStudent) {
                assignedRole = Role.ADMIN;
                if (email != null && !email.isBlank() && email.contains("@")) {
                    name = email.split("@")[0];
                } else {
                    name = email;
                }
            } else {
                if (student == null) {
                    log.warn("Student info missing in UserInfo: {}", userInfo);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "학생 정보가 없습니다.");
                }

                name = student.getName();
                studentNumber = student.getStudentNumber() != null ? String.valueOf(student.getStudentNumber()).trim() : null;
                try {
                    Object g = student.getClass().getMethod("getGrade").invoke(student);
                    if (g != null) grade = String.valueOf(g).trim();
                } catch (Exception ignored) {
                }

                if (email == null || email.isBlank() || name == null || name.isBlank() || studentNumber == null || studentNumber.isBlank()) {
                    log.warn("Invalid student info: email='{}', name='{}', studentNumber='{}'", email, name, studentNumber);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "학생 정보가 유효하지 않습니다.");
                }

                try {
                    Long.parseLong(studentNumber);
                } catch (NumberFormatException e) {
                    log.warn("Invalid studentNumber format: {}", studentNumber);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "학번 형식이 유효하지 않습니다.", e);
                }

                if ("3".equals(grade)) {
                    log.warn("Grade 3 student login blocked: email='{}', name='{}'", email, name);
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "3학년은 로그인할 수 없습니다.");
                }
            }

            UserEntity user = findOrCreateUser(email, name, studentNumber, assignedRole, grade);
            String accessToken = jwtTokenProvider.createToken(user);

            return new OAuthSignInResponse(
                    accessToken,
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getStudentNumber(),
                    user.getGrade(),
                    user.getRole().name(),
                    user.getAdminRole() != null ? user.getAdminRole().name() : null,
                    user.getAdminTeam(),
                    user.isGradeHead(),
                    user.getClientRole() != null ? user.getClientRole().name() : null);
        } catch (DataGsmException e) {
            log.warn("DataGSM OAuth error: status={}, message={}", e.getStatusCode(), e.getMessage());
        throw new ResponseStatusException(resolveStatus(e.getStatusCode()), "OAuth 인증에 실패했습니다.", e);
        }
    }

    private HttpStatus resolveStatus(int statusCode) {
        HttpStatus resolved = HttpStatus.resolve(statusCode);
        return resolved != null ? resolved : HttpStatus.BAD_GATEWAY;
    }

    public void assertAllowedRedirectUri(String redirectUri) {
        if (redirectUri == null || redirectUri.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "허용되지 않은 redirectUri입니다.");
        }
        List<String> allowed = oauthProperties.getDatagsm().getRedirectUris();

        if (!allowed.contains(redirectUri)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "허용되지 않은 redirectUri입니다.");
        }
    }

    @Transactional(readOnly = true)
    public UserEntity requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse buildCurrentUserResponse(UserEntity user) {
        Long projectId = null;
        String clientTeam = null;

        List<ProjectMember> members = projectMemberRepository.findAllByUserId(user.getId());
        if (members != null && !members.isEmpty()) {
            ProjectMember pick = members.stream()
                    .filter(m -> m.getRole() == ClientRole.LEADER)
                    .findFirst()
                    .orElse(members.get(0));
            if (pick != null && pick.getProject() != null) {
                projectId = pick.getProject().getId();
                clientTeam = pick.getProject().getTeamName();
            }
        }

        return new CurrentUserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getStudentNumber(),
                user.getGrade(),
                user.getEffectiveRole().name(),
                user.getAdminRole() != null ? user.getAdminRole().name() : null,
                user.getAdminTeam(),
                user.isGradeHead(),
                user.getClientRole() != null ? user.getClientRole().name() : null,
                projectId,
                clientTeam,
                null
        );
    }

    @Transactional
    public CurrentUserResponse updateCurrentUser(AuthenticatedUser caller, UpdateCurrentUserRequest request) {
        if (caller == null || caller.userId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }

        UserEntity user = requireUser(caller.userId());
        boolean changed = false;

        String newName = (request.name() != null && !request.name().isBlank()) ? request.name() : user.getName();
        String newStudentNumber = (request.studentNumber() != null && !request.studentNumber().isBlank()) ? request.studentNumber() : user.getStudentNumber();

        if (request.studentNumber() != null && !request.studentNumber().isBlank()) {
            try {
                Long.parseLong(request.studentNumber());
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "학번 형식이 유효하지 않습니다.");
            }
        }

        if (!newName.equals(user.getName()) || (newStudentNumber != null && !newStudentNumber.equals(user.getStudentNumber())) || (newStudentNumber == null && user.getStudentNumber() != null)) {
            user.updateProfile(newName, newStudentNumber);
            changed = true;
        }

        if (request.adminRole() != null || request.adminTeam() != null) {
            if (caller.role() == null || caller.role() != Role.ADMIN) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자 정보는 관리자(선생님)만 수정할 수 있습니다.");
            }

            AdminRole newAdminRole = request.adminRole() != null ? request.adminRole() : user.getAdminRole();
            String validationMessage = AdminRole.subjectTeacherValidationMessage(newAdminRole);
            if (validationMessage != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, validationMessage);
            }

            String newAdminTeam = request.adminTeam() != null ? request.adminTeam() : user.getAdminTeam();

            user.updateAdminAdditionalInfo(newAdminRole, newName, newAdminTeam, user.isGradeHead());
            changed = true;
        }

        if (request.clientRole() != null) {
            if (caller.role() == Role.ADMIN) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "학생 전용 필드는 관리자(선생님)가 수정할 수 없습니다.");
            }
            user.updateClientAdditionalInfo(request.clientRole());
            changed = true;
        }

        if (changed) {
            userRepository.save(user);
        }

        String newAccessToken = jwtTokenProvider.createToken(user);

        return buildCurrentUserResponseWithToken(user, newAccessToken);
    }

    private UserEntity findOrCreateUser(String email, String name, String studentNumber, Role role, String grade) {
        return userRepository.findByEmail(email)
                .map(existing -> {
                    existing.updateProfile(name, studentNumber, grade);
                    if (existing.getRole() != role) {
                        existing.setRole(role);
                    }
                    return existing;
                })
                .orElseGet(() -> userRepository.save(new UserEntity(email, name, studentNumber, role, grade)));
    }

    private UserEntity findOrCreateUser(String email, String name, String studentNumber, Role role) {
        return findOrCreateUser(email, name, studentNumber, role, null);
    }

    @Transactional
    public OAuthSignInResponse signInWithGoogle(String accessToken, Map userInfo) {
        String email = userInfo.get("email") != null ? userInfo.get("email").toString() : null;
        String name = userInfo.get("name") != null ? userInfo.get("name").toString() : null;
        String studentNumber = null;
        String grade = null;

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Google 사용자 이메일을 가져오지 못했습니다.");
        }

        Role assignedRole = Role.ADMIN;

        UserEntity user = findOrCreateUser(email, name, studentNumber, assignedRole, grade);
        String token = jwtTokenProvider.createToken(user);

        return new OAuthSignInResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getStudentNumber(),
                user.getGrade(),
                user.getRole().name(),
                user.getAdminRole() != null ? user.getAdminRole().name() : null,
                user.getAdminTeam(),
                user.isGradeHead(),
                user.getClientRole() != null ? user.getClientRole().name() : null);
    }

    private CurrentUserResponse buildCurrentUserResponseWithToken(UserEntity user, String accessToken) {
        Long projectId = null;
        String clientTeam = null;

        List<ProjectMember> members = projectMemberRepository.findAllByUserId(user.getId());
        if (members != null && !members.isEmpty()) {
            ProjectMember pick = members.stream()
                    .filter(m -> m.getRole() == ClientRole.LEADER)
                    .findFirst()
                    .orElse(members.get(0));
            if (pick != null && pick.getProject() != null) {
                projectId = pick.getProject().getId();
                clientTeam = pick.getProject().getTeamName();
            }
        }

        return new CurrentUserResponse(
                user.getId(), user.getEmail(), user.getName(), user.getStudentNumber(), user.getGrade(),
                user.getEffectiveRole().name(),
                user.getAdminRole() != null ? user.getAdminRole().name() : null,
                user.getAdminTeam(), user.isGradeHead(),
                user.getClientRole() != null ? user.getClientRole().name() : null,
                projectId,
                clientTeam,
                accessToken
        );
    }

    @Transactional(readOnly = true)
    public String renewToken(Long userId) {
        UserEntity user = requireUser(userId);
        return jwtTokenProvider.createToken(user);
    }
}
