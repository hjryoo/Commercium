package com.commercium.config.security;

import com.commercium.config.properties.JwtProperties;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Component
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class JwtConfig {

    private final JwtProperties jwtProperties;

    private String generateAccessToken(String userId, String secret, long seconds) {
        var key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        var now = new Date();
        var exp = new Date(now.getTime() + seconds * 1000);

        return Jwts.builder()
                .subject(userId)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(String userId) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.getRefreshTokenExpiry(), ChronoUnit.SECONDS);

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .claim("type", "REFRESH")
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecret())
                .compact();
    }
}

