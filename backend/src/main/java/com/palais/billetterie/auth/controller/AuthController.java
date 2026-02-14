package com.palais.billetterie.auth.controller;

import com.palais.billetterie.auth.dto.LoginRequest;
import com.palais.billetterie.auth.dto.RefreshRequest;
import com.palais.billetterie.auth.dto.RegisterRequest;
import com.palais.billetterie.auth.dto.TokenResponse;
import com.palais.billetterie.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        return ResponseEntity.ok(authService.refresh(req.refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody RefreshRequest req) {
        authService.logout(req.refreshToken);
        return ResponseEntity.ok("Déconnecté");
    }
}
