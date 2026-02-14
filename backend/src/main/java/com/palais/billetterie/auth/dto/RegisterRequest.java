package com.palais.billetterie.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class RegisterRequest {
    @NotBlank(message = "Nom requis")
    public String name;

    @Email(message = "Email invalide")
    @NotBlank(message = "Email requis")
    public String email;

    @NotBlank(message = "Mot de passe requis")
    public String password;
}
