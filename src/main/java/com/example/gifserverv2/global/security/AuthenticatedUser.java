package com.example.gifserverv2.global.security;

import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.domain.user.entity.AdminRole;
import com.example.gifserverv2.domain.user.entity.ClientRole;

public record AuthenticatedUser(
        Long userId,
        String email,
        String name,
        String studentNumber,
        Role role,
        AdminRole adminRole,
        String adminTeam,
        ClientRole clientRole) {
}
