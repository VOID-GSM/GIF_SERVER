package com.example.gifserverv2.auth.domain.auth.service;

import com.example.gifserverv2.auth.domain.auth.dto.request.OAuthSignInRequest;
import com.example.gifserverv2.auth.domain.auth.dto.response.OAuthSignInResponse;
import com.example.gifserverv2.auth.domain.user.entity.Role;
import com.example.gifserverv2.auth.domain.user.entity.UserEntity;
import com.example.gifserverv2.auth.domain.user.repository.UserRepository;
import com.example.gifserverv2.auth.global.security.JwtTokenProvider;
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

            if (!userInfo.isStudent()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "학생만 로그인할 수 있습니다.");
            }

            Student student = userInfo.getStudent();
            if (student == null || student.getName() == null || student.getStudentNumber() == null || userInfo.getEmail() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "학생 정보가 유효하지 않습니다.");
            }

            UserEntity user = findOrCreateUser(userInfo.getEmail(), student.getName(), String.valueOf(student.getStudentNumber()));
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

    private UserEntity findOrCreateUser(String email, String name, String studentNumber) {
        return userRepository.findByEmail(email)
                .map(existing -> {
                    existing.updateProfile(name, studentNumber);
                    return existing;
                })
                .orElseGet(() -> userRepository.save(new UserEntity(email, name, studentNumber, Role.USER)));
    }
}
