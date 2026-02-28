package com.palais.billetterie.security.controller;

import com.palais.billetterie.security.jwt.JwtService;
import com.palais.billetterie.user.domain.Role;
import com.palais.billetterie.user.domain.User;
import com.palais.billetterie.user.repository.UserRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public record RegisterRequest(@NotBlank String name, @Email String email, @NotBlank String password) {}
    public record LoginRequest(@Email String email, @NotBlank String password) {}
    public record AuthResponse(String accessToken, String refreshToken) {}

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest req) {
        userRepository.findByEmail(req.email()).ifPresent(u -> { throw new com.palais.billetterie.common.exceptions.ConflictException("Email déjà utilisé"); });
        User user = User.builder()
                .name(req.name())
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(Role.USER)
                .createdAt(Instant.now())
                .build();
        userRepository.save(user);
        String access = jwtService.generateAccessToken(user.getEmail(), Map.of("role", user.getRole().name()));
        String refresh = jwtService.generateRefreshToken(user.getEmail());
        return ResponseEntity.ok(new AuthResponse(access, refresh));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        User user = userRepository.findByEmail(req.email()).orElseThrow(() -> new com.palais.billetterie.common.exceptions.UnauthorizedException("Identifiants invalides"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new com.palais.billetterie.common.exceptions.UnauthorizedException("Identifiants invalides");
        }
        String access = jwtService.generateAccessToken(user.getEmail(), Map.of("role", user.getRole().name()));
        String refresh = jwtService.generateRefreshToken(user.getEmail());
        return ResponseEntity.ok(new AuthResponse(access, refresh));
    }
}
