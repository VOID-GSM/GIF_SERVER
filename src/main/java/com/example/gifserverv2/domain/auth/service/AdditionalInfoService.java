package com.example.gifserverv2.domain.auth.service;

import com.example.gifserverv2.domain.auth.dto.request.AdminAdditionalInfoRequest;
import com.example.gifserverv2.domain.auth.dto.request.ClientAdditionalInfoRequest;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdditionalInfoService {

    private final AuthService authService;

    public AdditionalInfoService(AuthService authService) {
        this.authService = authService;
    }

    @Transactional
    public void updateAdminAdditionalInfo(Long userId, AdminAdditionalInfoRequest request) {
        UserEntity user = authService.requireUser(userId);
        if (user.getEffectiveRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 관리자 추가 정보를 입력할 수 있습니다.");
        }

        user.updateAdminAdditionalInfo(request.adminRole());
    }

    @Transactional
    public void updateClientAdditionalInfo(Long userId, ClientAdditionalInfoRequest request) {
        UserEntity user = authService.requireUser(userId);
        if (user.getEffectiveRole() != Role.CLIENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "학생만 학생 추가 정보를 입력할 수 있습니다.");
        }

        user.updateClientAdditionalInfo(request.clientRole());
    }
}
