package com.example.gifserverv2.global.security;

import com.example.gifserverv2.domain.user.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long validityInMinutes;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long validityInMinutes) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.validityInMinutes = validityInMinutes;
    }

    public String createToken(UserEntity user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + validityInMinutes * 60 * 1000);

        var builder = Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("studentNumber", user.getStudentNumber())
                .claim("role", user.getEffectiveRole().name())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey);

        if (user.getAdminRole() != null) {
            builder.claim("adminRole", user.getAdminRole().name());
        }
        if (user.getAdminTeam() != null) {
            builder.claim("adminTeam", user.getAdminTeam());
        }
        if (user.getClientRole() != null) {
            builder.claim("clientRole", user.getClientRole().name());
        }

        return builder.compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }
}
