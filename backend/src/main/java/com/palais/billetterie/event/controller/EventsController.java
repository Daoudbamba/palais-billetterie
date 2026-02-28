package com.palais.billetterie.event.controller;

import com.palais.billetterie.event.domain.Event;
import com.palais.billetterie.event.repository.EventRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class EventsController {

    private final EventRepository repository;

    public EventsController(EventRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<Event>> list() {
        List<Event> all = repository.findAll();
        return ResponseEntity.ok(all);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> get(@PathVariable("id") UUID id) {
        Event ev = repository.findById(id).orElseThrow();
        return ResponseEntity.ok(ev);
    }
}