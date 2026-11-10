package com.example.gifserverv2.global.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] SCHOOL_ROLES = {"USER", "ADMIN"};

    private final TokenAuthenticationFilter tokenAuthenticationFilter;
    private final List<String> allowedOrigins;

    public SecurityConfig(
            TokenAuthenticationFilter tokenAuthenticationFilter,
            @Value("${app.cors.allowed-origins:http://localhost:3000}") String allowedOrigins) {
        this.tokenAuthenticationFilter = tokenAuthenticationFilter;
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .collect(Collectors.toList());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler(new JsonAccessDeniedHandler()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/dg/start", "/api/auth/dg/callback").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/google/start", "/api/auth/google/callback").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/signin").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/files/**").permitAll()
                        // 비로그인 방문자도 조회수를 올릴 수 있다. 아래 POST 인증 규칙보다 반드시 먼저 와야 한다.
                        .requestMatchers(HttpMethod.POST, "/api/project/*/view").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/project/filter").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/project/{projectId:\\d+}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/project/{projectId:\\d+}/comments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
                        // 외부 사용자(EXTERNAL)는 아래 조회·참여 API만 사용할 수 있다.
                        .requestMatchers(HttpMethod.GET, "/api/project/me", "/api/project/admin").hasAnyRole(SCHOOL_ROLES)
                        .requestMatchers(HttpMethod.GET, "/api/project/*", "/api/project/*/links",
                                "/api/project/*/engagement", "/api/project/*/comments").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/project/*/comments", "/api/project/*/like").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/project/*/comments/*", "/api/project/*/like").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/users/me").authenticated()
                        // 마이페이지. GitHub 연동(/api/users/me/github/**)은 재학생 전용이라 열지 않는다.
                        .requestMatchers("/api/users/me/mypage", "/api/users/me/profile-image",
                                "/api/users/me/links", "/api/users/me/links/*").authenticated()
                        // 다른 사용자의 공개 프로필
                        .requestMatchers(HttpMethod.GET, "/api/users/{userId:\\d+}/profile").authenticated()
                        // Thyn 꾸미기(아이템)
                        .requestMatchers("/api/items/**").authenticated()
                        .requestMatchers("/api/users/me/mypage", "/api/users/me/profile-image",
                                "/api/users/me/links", "/api/users/me/links/*", "/api/users/me/items").authenticated()
                        .requestMatchers("/api/notifications/**", "/api/push/**").authenticated()
                        .anyRequest().hasAnyRole(SCHOOL_ROLES))
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}