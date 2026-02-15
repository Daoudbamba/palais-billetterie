package com.palais.billetterie.event.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventsController {

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        return ResponseEntity.ok(List.of(
                Map.of("id", 1, "title", "Concert A", "capacity", 500),
                Map.of("id", 2, "title", "Conference B", "capacity", 300)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(Map.of("id", id, "title", "Event "+id, "capacity", 500));
    }
}