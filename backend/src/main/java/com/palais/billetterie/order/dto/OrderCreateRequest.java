package com.palais.billetterie.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class OrderCreateRequest {
    @NotNull(message = "L'utilisateur est obligatoire")
    private UUID userId;

    @NotNull(message = "L'événement est obligatoire")
    private UUID eventId;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être supérieur à 0")
    private Double amount;
}
