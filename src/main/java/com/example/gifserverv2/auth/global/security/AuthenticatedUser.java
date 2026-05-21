package com.example.gifserverv2.auth.global.security;

import com.example.gifserverv2.auth.domain.user.entity.Role;

public record AuthenticatedUser (Long userId, String email, String name, String studentNumber, Role role) {
}
