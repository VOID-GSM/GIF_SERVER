package com.example.gifserverv2.global.security;

import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.domain.user.entity.AdminRole;
import com.example.gifserverv2.domain.user.entity.ClientRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;

    public TokenAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = jwtTokenProvider.parseClaims(token);
                Long userId = parseUserId(claims.getSubject());
                String email = claims.get("email", String.class);
                String name = claims.get("name", String.class);
                String studentNumber = claims.get("studentNumber", String.class);
                String roleClaim = claims.get("role", String.class);
                if (roleClaim == null || roleClaim.isBlank()) {
                    throw new IllegalArgumentException("Missing role claim");
                }
                Role role = parseRole(roleClaim);
                AdminRole adminRole = parseAdminRole(claims.get("adminRole", String.class));
                ClientRole clientRole = parseClientRole(claims.get("clientRole", String.class));

                AuthenticatedUser principal = new AuthenticatedUser(userId, email, name, studentNumber, role, adminRole, clientRole);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (NumberFormatException e) {
                log.debug("Invalid JWT subject ignored: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            } catch (JwtException | IllegalArgumentException e) {
                log.debug("Invalid JWT ignored: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Long parseUserId(String subject) {
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Missing subject claim");
        }

        return Long.valueOf(subject.trim());
    }

    private Role parseRole(String roleClaim) {
        if ("USER".equals(roleClaim)) {
            return Role.CLIENT;
        }

        return Role.valueOf(roleClaim);
    }

    private AdminRole parseAdminRole(String adminRoleClaim) {
        if (adminRoleClaim == null || adminRoleClaim.isBlank()) {
            return null;
        }

        return AdminRole.valueOf(adminRoleClaim);
    }

    private ClientRole parseClientRole(String clientRoleClaim) {
        if (clientRoleClaim == null || clientRoleClaim.isBlank()) {
            return null;
        }

        return ClientRole.valueOf(clientRoleClaim);
    }
}
