package com.palais.billetterie.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${app.jwt.accessSecret}")
    private String accessSecret;

    @Value("${app.jwt.refreshSecret}")
    private String refreshSecret;

    @Value("${app.jwt.accessTtlMinutes}")
    private long accessTtlMinutes;

    @Value("${app.jwt.refreshTtlDays}")
    private long refreshTtlDays;

    private Key accessKey() {
        return Keys.hmacShaKeyFor(deriveKey(accessSecret));
    }

    private Key refreshKey() {
        return Keys.hmacShaKeyFor(deriveKey(refreshSecret));
    }

    private byte[] deriveKey(String secret) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            return md.digest(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            byte[] raw = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] out = new byte[32];
            for (int i = 0; i < out.length; i++) out[i] = raw[i % raw.length];
            return out;
        }
    }

    public String generateAccessToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTtlMinutes * 60);
        return Jwts.builder()
                .setSubject(subject)
                .addClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(accessKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String subject) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(refreshTtlDays * 24 * 60 * 60);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(refreshKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseAccessToken(String token) {
        return Jwts.parserBuilder().setSigningKey(accessKey()).build().parseClaimsJws(token).getBody();
    }
}
