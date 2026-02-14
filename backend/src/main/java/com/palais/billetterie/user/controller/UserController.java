package com.palais.billetterie.user.controller;

import com.palais.billetterie.user.domain.User;
import com.palais.billetterie.user.domain.Role;
import com.palais.billetterie.user.dto.UserCreateRequest;
import com.palais.billetterie.user.dto.UserResponse;
import jakarta.validation.Valid;
import com.palais.billetterie.user.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserResponse> list() {
        return service.getAll().stream().map(UserResponse::of).toList();
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable("id") UUID id) {
        return UserResponse.of(service.getById(id));
    }

    @PostMapping
    public UserResponse create(@RequestBody @Valid UserCreateRequest req) {
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .role(req.getRole() == null ? Role.USER : req.getRole())
                .build();
        return UserResponse.of(service.create(user));
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable("id") UUID id, @RequestBody @Valid UserCreateRequest req) {
        User patch = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .role(req.getRole() == null ? Role.USER : req.getRole())
                .build();
        return UserResponse.of(service.update(id, patch));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") UUID id) {
        service.delete(id);
    }
}