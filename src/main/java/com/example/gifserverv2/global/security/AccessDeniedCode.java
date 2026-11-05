package com.example.gifserverv2.global.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public enum AccessDeniedCode {
    EXTERNAL_USER_FORBIDDEN("외부 사용자는 이용할 수 없는 기능입니다."),
    ACCESS_DENIED("접근 권한이 없습니다.");

    private final String message;

    AccessDeniedCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public static AccessDeniedCode current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean external = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_EXTERNAL".equals(authority.getAuthority()));
        return external ? EXTERNAL_USER_FORBIDDEN : ACCESS_DENIED;
    }
}
