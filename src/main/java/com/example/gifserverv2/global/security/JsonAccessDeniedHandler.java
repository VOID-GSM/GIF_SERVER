package com.example.gifserverv2.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    public static final String EXTERNAL_USER_FORBIDDEN = "EXTERNAL_USER_FORBIDDEN";
    public static final String ACCESS_DENIED = "ACCESS_DENIED";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        boolean external = isExternal(SecurityContextHolder.getContext().getAuthentication());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", HttpStatus.FORBIDDEN.getReasonPhrase());
        body.put("code", external ? EXTERNAL_USER_FORBIDDEN : ACCESS_DENIED);
        body.put("message", external ? "외부 사용자는 이용할 수 없는 기능입니다." : "접근 권한이 없습니다.");
        body.put("path", request.getRequestURI());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), body);
    }

    private boolean isExternal(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_EXTERNAL".equals(authority.getAuthority()));
    }
}
