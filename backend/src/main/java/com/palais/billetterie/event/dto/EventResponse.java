package com.palais.billetterie.event.dto;

import com.palais.billetterie.event.domain.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class EventResponse {
    private UUID id;
    private String title;
    private String description;
    private Instant startDateTime;
    private Instant endDateTime;
    private String venue;
    private Integer capacity;
    private Instant createdAt;

    public static EventResponse of(Event e) {
        return new EventResponse(e.getId(), e.getTitle(), e.getDescription(), e.getStartDateTime(), e.getEndDateTime(), e.getVenue(), e.getCapacity(), e.getCreatedAt());
    }
}
