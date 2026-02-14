package com.palais.billetterie.security.jwt;

import com.palais.billetterie.user.domain.User;
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

    private final Key accessKey;
    private final Key refreshKey;
    private final long accessTtlMillis;
    private final long refreshTtlMillis;

    public JwtService(@Value("${app.jwt.accessSecret}") String accessSecret,
                      @Value("${app.jwt.refreshSecret}") String refreshSecret,
                      @Value("${app.jwt.accessTtlMinutes}") long accessTtlMinutes,
                      @Value("${app.jwt.refreshTtlDays}") long refreshTtlDays) {
        this.accessKey = Keys.hmacShaKeyFor(deriveKey(accessSecret));
        this.refreshKey = Keys.hmacShaKeyFor(deriveKey(refreshSecret));
        this.accessTtlMillis = accessTtlMinutes * 60_000L;
        this.refreshTtlMillis = refreshTtlDays * 24L * 60L * 60_000L;
    }

    private byte[] deriveKey(String secret) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            return md.digest(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            // Fallback to repeating bytes to reach 32 length
            byte[] raw = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] out = new byte[32];
            for (int i = 0; i < out.length; i++) {
                out[i] = raw[i % raw.length];
            }
            return out;
        }
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(accessTtlMillis)))
                .addClaims(Map.of(
                        "email", user.getEmail(),
                        "role", user.getRole() == null ? "USER" : user.getRole().name()
                ))
                .signWith(accessKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user, String tokenId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setId(tokenId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(refreshTtlMillis)))
                .signWith(refreshKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseAccessToken(String token) {
        return Jwts.parserBuilder().setSigningKey(accessKey).build()
                .parseClaimsJws(token).getBody();
    }

    public Claims parseRefreshToken(String token) {
        return Jwts.parserBuilder().setSigningKey(refreshKey).build()
                .parseClaimsJws(token).getBody();
    }
}
