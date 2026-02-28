package com.palais.billetterie.event.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "events",
    indexes = {
        @Index(name = "idx_events_createdat", columnList = "createdAt"),
        @Index(name = "idx_events_start_end", columnList = "startDateTime, endDateTime")
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Event {

    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank(message = "Le titre est obligatoire")
    private String title;

    @Column(length = 2000)
    private String description;

    @NotNull(message = "La date de début est obligatoire")
    private Instant startDateTime;

    @NotNull(message = "La date de fin est obligatoire")
    private Instant endDateTime;

    private String venue;

    @Min(value = 1, message = "La capacité doit être supérieure à 0")
    private Integer capacity;

    @AssertTrue(message = "La date de début doit être avant la date de fin")
    public boolean isDateRangeValid() {
        if (startDateTime == null || endDateTime == null) return false;
        return startDateTime.isBefore(endDateTime);
    }

    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}