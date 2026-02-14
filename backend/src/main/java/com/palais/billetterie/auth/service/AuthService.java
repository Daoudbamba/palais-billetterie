package com.palais.billetterie.auth.service;

import com.palais.billetterie.auth.dto.LoginRequest;
import com.palais.billetterie.auth.dto.RegisterRequest;
import com.palais.billetterie.auth.dto.TokenResponse;
import com.palais.billetterie.common.exceptions.BadRequestException;
import com.palais.billetterie.security.jwt.JwtService;
import com.palais.billetterie.security.jwt.RefreshToken;
import com.palais.billetterie.security.jwt.RefreshTokenRepository;
import com.palais.billetterie.user.domain.Role;
import com.palais.billetterie.user.domain.User;
import com.palais.billetterie.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public TokenResponse register(RegisterRequest req) {
        userRepository.findByEmail(req.email).ifPresent(u -> {
            throw new BadRequestException("Email déjà utilisé");
        });
        User user = User.builder()
                .name(req.name)
                .email(req.email)
                .passwordHash(passwordEncoder.encode(req.password))
                .role(Role.USER)
                .createdAt(Instant.now())
                .build();
        user = userRepository.save(user);
        return issueTokens(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable"));
        if (!passwordEncoder.matches(req.password, user.getPasswordHash())) {
            throw new BadRequestException("Identifiants invalides");
        }
        return issueTokens(user);
    }

    @Transactional
    public TokenResponse refresh(String refreshTokenStr) {
        Claims claims = jwtService.parseRefreshToken(refreshTokenStr);
        String jti = claims.getId();
        RefreshToken rt = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new BadRequestException("Refresh token invalide"));
        if (rt.isRevoked() || rt.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Refresh token expiré ou révoqué");
        }
        // Rotation: revoke old and issue new token
        rt.setRevoked(true);
        refreshTokenRepository.save(rt);

        User user = rt.getUser();
        return issueTokens(user);
    }

    @Transactional
    public void logout(String refreshTokenStr) {
        refreshTokenRepository.findByToken(refreshTokenStr).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    private TokenResponse issueTokens(User user) {
        String access = jwtService.generateAccessToken(user);
        // Persist refresh token entity
        String tokenId = UUID.randomUUID().toString();
        String refresh = jwtService.generateRefreshToken(user, tokenId);
        RefreshToken rt = RefreshToken.builder()
                .user(user)
                .token(refresh)
                .expiresAt(Instant.now().plusSeconds(14 * 24 * 3600))
                .revoked(false)
                .createdAt(Instant.now())
                .build();
        refreshTokenRepository.save(rt);
        return new TokenResponse(access, refresh);
    }
}
