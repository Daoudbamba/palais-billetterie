package com.palais.billetterie.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class RefreshRequest {
    @NotBlank(message = "Refresh token requis")
    public String refreshToken;
}
