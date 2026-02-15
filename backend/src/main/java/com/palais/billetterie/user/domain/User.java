package com.palais.billetterie.user.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.Instant;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank(message = "Le nom est obligatoire")
    private String name;

    @Email(message = "Format d'email invalide")
    @NotBlank(message = "L'email est obligatoire")
    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    @JsonIgnore
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
 