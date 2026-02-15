package com.palais.billetterie.ticket.controller;

import com.palais.billetterie.ticket.domain.Ticket;
import com.palais.billetterie.ticket.dto.TicketCreateRequest;
import com.palais.billetterie.ticket.dto.TicketUpdateRequest;
import com.palais.billetterie.ticket.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @GetMapping
    public List<Ticket> list() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Ticket get(@PathVariable("id") UUID id) {
        return service.getById(id);
    }

    @PostMapping
    public Ticket create(@RequestBody @Valid TicketCreateRequest req) {
        return service.create(req.getUserId(), req.getEventId(), req.getOrderId());
    }

    @PutMapping("/{id}")
    public Ticket update(@PathVariable("id") UUID id, @RequestBody @Valid TicketUpdateRequest req) {
        return service.update(id, req.getStatus());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") UUID id) {
        service.delete(id);
    }
}