package com.example.gifserverv2.domain.auth.service;

import com.example.gifserverv2.domain.auth.dto.request.AdminAdditionalInfoRequest;
import com.example.gifserverv2.domain.auth.dto.request.ClientAdditionalInfoRequest;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdditionalInfoService {

    private final AuthService authService;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public AdditionalInfoService(AuthService authService, ProjectRepository projectRepository, UserRepository userRepository) {
        this.authService = authService;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void updateAdminAdditionalInfo(Long userId, AdminAdditionalInfoRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
        if (user.getEffectiveRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "선생님만 선생님 추가 정보를 입력할 수 있습니다.");
        }

        String adminTeam = normalizeTeam(request.adminTeam());
        validateAdminTeam(user.getId(), adminTeam);

        user.updateAdminAdditionalInfo(request.adminRole(), adminTeam);
        userRepository.save(user);
    }

    @Transactional
    public void updateClientAdditionalInfo(Long userId, ClientAdditionalInfoRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
        if (user.getEffectiveRole() != Role.USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "학생만 학생 추가 정보를 입력할 수 있습니다.");
        }

        user.updateClientAdditionalInfo(request.clientRole());
        userRepository.save(user);
    }

    private String normalizeTeam(String adminTeam) {
        if (adminTeam == null) {
            return null;
        }

        String trimmed = adminTeam.trim();
        if (trimmed.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "담당 팀은 빈 값일 수 없습니다.");
        }

        return trimmed;
    }

    private void validateAdminTeam(Long userId, String adminTeam) {
        if (adminTeam == null) {
            return;
        }

        if (!projectRepository.existsByTeamName(adminTeam)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 팀입니다.");
        }

        if (userRepository.existsByAdminTeamAndIdNotAndAdminRoleIsNotNull(adminTeam, userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 담당 선생님이 배정된 팀입니다.");
        }
    }
}
