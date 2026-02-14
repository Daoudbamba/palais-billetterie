package com.palais.billetterie.user.dto;

import com.palais.billetterie.user.domain.Role;
import com.palais.billetterie.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private Instant createdAt;

    public static UserResponse of(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getPhone(), u.getRole(), u.getCreatedAt());
    }
}
