package com.palais.billetterie.event.controller;

import com.palais.billetterie.event.domain.Event;
import com.palais.billetterie.event.dto.EventCreateRequest;
import com.palais.billetterie.event.dto.EventResponse;
import jakarta.validation.Valid;
import com.palais.billetterie.event.service.EventService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class EventsController {

    private final EventService service;

    public EventsController(EventService service) {
        this.service = service;
    }

    @GetMapping
    public List<EventResponse> list() {
        return service.getAll().stream().map(EventResponse::of).toList();
    }

    @GetMapping("/{id}")
    public EventResponse get(@PathVariable("id") UUID id) {
        return EventResponse.of(service.getById(id));
    }

    @PostMapping
    public EventResponse create(@RequestBody @Valid EventCreateRequest req) {
        Event event = Event.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .startDateTime(req.getStartDateTime())
                .endDateTime(req.getEndDateTime())
                .venue(req.getVenue())
                .capacity(req.getCapacity())
                .build();
        return EventResponse.of(service.create(event));
    }

    @PutMapping("/{id}")
    public EventResponse update(@PathVariable("id") UUID id, @RequestBody @Valid EventCreateRequest req) {
        Event patch = Event.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .startDateTime(req.getStartDateTime())
                .endDateTime(req.getEndDateTime())
                .venue(req.getVenue())
                .capacity(req.getCapacity())
                .build();
        return EventResponse.of(service.update(id, patch));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") UUID id) {
        service.delete(id);
    }
}