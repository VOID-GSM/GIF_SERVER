package com.example.gifserverv2.domain.auth.service;

import com.example.gifserverv2.domain.auth.dto.request.AdminAdditionalInfoRequest;
import com.example.gifserverv2.domain.auth.dto.request.ClientAdditionalInfoRequest;
import com.example.gifserverv2.domain.user.entity.AdminRole;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdditionalInfoService {

    private final UserRepository userRepository;

    @Transactional
    public void updateAdminAdditionalInfo(Long userId, AdminAdditionalInfoRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        if (user.getEffectiveRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "선생님만 선생님 추가 정보를 입력할 수 있습니다.");
        }

        String validationMessage = AdminRole.subjectTeacherValidationMessage(request.adminRole());
        if (validationMessage != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, validationMessage);
        }

        user.updateAdminAdditionalInfo(
                request.adminRole(),
                request.name(),
                null,
                request.gradeHead()
        );
    }

    @Transactional
    public void updateClientAdditionalInfo(Long userId, ClientAdditionalInfoRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        if (user.getEffectiveRole() != Role.USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "학생만 학생 추가 정보를 입력할 수 있습니다.");
        }

        user.updateClientAdditionalInfo(request.clientRole());
    }
}