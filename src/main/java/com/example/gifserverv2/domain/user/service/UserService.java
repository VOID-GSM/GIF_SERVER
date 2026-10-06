package com.example.gifserverv2.domain.user.service;

import com.example.gifserverv2.domain.auth.dto.request.UpdateCurrentUserRequest;
import com.example.gifserverv2.domain.auth.dto.response.CurrentUserResponse;
import com.example.gifserverv2.domain.auth.dto.response.GithubUserInfo;
import com.example.gifserverv2.domain.auth.service.GithubOAuthClient;
import com.example.gifserverv2.domain.coin.service.GithubFetchService;
import com.example.gifserverv2.domain.project.entity.ProjectMember;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import com.example.gifserverv2.domain.user.entity.AdminRole;
import com.example.gifserverv2.domain.user.entity.ClientRole;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import com.example.gifserverv2.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final GithubOAuthClient githubOAuthClient;
    private final GithubFetchService githubFetchService;
    private final ProjectMemberRepository projectMemberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public UserEntity requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
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

    @Transactional
    public CurrentUserResponse connectGithub(AuthenticatedUser caller, String code) {
        if (caller == null || caller.userId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }

        UserEntity user = requireUser(caller.userId());

        String githubAccessToken = githubOAuthClient.getAccessToken(code);

        GithubUserInfo githubUser = githubOAuthClient.getUserInfo(githubAccessToken);

        userRepository.findByGithubUsername(githubUser.login())
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(user.getId())) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 다른 계정에 연동된 GitHub 계정입니다.");
                    }
                });

        int initialCommitCount = githubFetchService.getCommitCount(githubUser.login(), githubAccessToken);

        user.connectGithub(githubUser.login(), githubUser.avatarUrl(), githubAccessToken, initialCommitCount);

        return buildCurrentUserResponse(user);
    }

    @Transactional
    public CurrentUserResponse disconnectGithub(AuthenticatedUser caller) {
        if (caller == null || caller.userId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }

        UserEntity user = requireUser(caller.userId());
        user.disconnectGithub();

        return buildCurrentUserResponse(user);
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse buildCurrentUserResponse(UserEntity user) {
        return buildCurrentUserResponseWithToken(user, null);
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
                user.getGithubUsername(),
                user.getGithubAvatarUrl(),
                user.getCoinBalance(),
                accessToken
        );
    }
}