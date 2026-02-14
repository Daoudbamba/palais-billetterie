package com.palais.billetterie.event.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class EventCreateRequest {
    @NotBlank(message = "Le titre est obligatoire")
    private String title;

    private String description;

    private String venue;

    @NotNull(message = "La date de début est obligatoire")
    private Instant startDateTime;

    @NotNull(message = "La date de fin est obligatoire")
    private Instant endDateTime;

    @Min(value = 1, message = "La capacité doit être supérieure à 0")
    private Integer capacity;
}
