package com.example.gifserverv2.domain.auth.service;

import com.example.gifserverv2.domain.auth.dto.request.OAuthSignInRequest;
import com.example.gifserverv2.domain.auth.dto.response.OAuthSignInResponse;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import com.example.gifserverv2.global.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import team.themoment.datagsm.sdk.oauth.DataGsmOAuthClient;
import team.themoment.datagsm.sdk.oauth.exception.DataGsmException;
import team.themoment.datagsm.sdk.oauth.model.Student;
import team.themoment.datagsm.sdk.oauth.model.TokenResponse;
import team.themoment.datagsm.sdk.oauth.model.UserInfo;

import java.util.Set;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final DataGsmOAuthClient dataGsmOAuthClient;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${oauth.datagsm.redirect-uris}")
    private Set<String> allowedRedirectUris;

    public AuthService(DataGsmOAuthClient dataGsmOAuthClient, UserRepository userRepository,
                       JwtTokenProvider jwtTokenProvider) {
        this.dataGsmOAuthClient = dataGsmOAuthClient;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
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

            boolean isStudent = userInfo.isStudent();
            Student student = userInfo.getStudent();
            String email = userInfo.getEmail();

            String name = null;
            String studentNumber = null;
            Role assignedRole = Role.USER;

            // try to read role string from userInfo using reflection (SDK may expose getRole() or role())
            String roleFromSdk = null;
            try {
                var m = userInfo.getClass().getMethod("getRole");
                roleFromSdk = (String) m.invoke(userInfo);
            } catch (Exception e1) {
                try {
                    var m2 = userInfo.getClass().getMethod("role");
                    roleFromSdk = (String) m2.invoke(userInfo);
                } catch (Exception ignored) {
                }
            }

            if (!isStudent) {
                if ("ADMIN".equalsIgnoreCase(roleFromSdk)) {
                    assignedRole = Role.ADMIN;
                    // try to get name from userInfo (may be available as getName() or name())
                    try {
                        var m = userInfo.getClass().getMethod("getName");
                        name = (String) m.invoke(userInfo);
                    } catch (Exception e) {
                        try {
                            var m2 = userInfo.getClass().getMethod("name");
                            name = (String) m2.invoke(userInfo);
                        } catch (Exception ignored) {
                        }
                    }
                    if (name == null || name.isBlank()) {
                        name = email != null ? email.split("@")[0] : null;
                    }
                } else {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "학생만 로그인할 수 있습니다.");
                }
            } else {
                if (student == null) {
                    log.warn("Student info missing in UserInfo: {}", userInfo);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "학생 정보가 없습니다.");
                }
                name = student.getName();
                studentNumber = student.getStudentNumber() != null ? String.valueOf(student.getStudentNumber()).trim() : null;

                if (email == null || email.isBlank() || name == null || name.isBlank() || studentNumber == null || studentNumber.isBlank()) {
                    log.warn("Invalid student info: email='{}', name='{}', studentNumber='{}'", email, name, studentNumber);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "학생 정보가 유효하지 않습니다.");
                }

                try {
                    Long.parseLong(studentNumber);
                } catch (NumberFormatException e) {
                    log.warn("Invalid studentNumber format: {}", studentNumber);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "학번 형식이 유효하지 않습니다.");
                }
            }

            UserEntity user = findOrCreateUser(email, name, studentNumber, assignedRole);
            String accessToken = jwtTokenProvider.createToken(user);

            return new OAuthSignInResponse(
                    accessToken,
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getStudentNumber(),
                    user.getRole().name());
        } catch (DataGsmException e) {
            log.warn("DataGSM OAuth error: status={}, message={}", e.getStatusCode(), e.getMessage());
            throw new ResponseStatusException(resolveStatus(e.getStatusCode()), "OAuth 인증에 실패했습니다.");
        }
    }

    private HttpStatus resolveStatus(int statusCode) {
        HttpStatus resolved = HttpStatus.resolve(statusCode);
        return resolved != null ? resolved : HttpStatus.BAD_GATEWAY;
    }

    public void assertAllowedRedirectUri(String redirectUri) {
        if (redirectUri == null || redirectUri.isBlank() || !allowedRedirectUris.contains(redirectUri)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "허용되지 않은 redirectUri입니다.");
        }
    }

    @Transactional(readOnly = true)
    public UserEntity requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
    }

    private UserEntity findOrCreateUser(String email, String name, String studentNumber, Role role) {
        return userRepository.findByEmail(email)
                .map(existing -> {
                    existing.updateProfile(name, studentNumber);
                    // update role if changed
                    if (existing.getRole() != role) {
                        existing.setRole(role);
                    }
                    return existing;
                })
                .orElseGet(() -> userRepository.save(new UserEntity(email, name, studentNumber, role)));
    }
}
