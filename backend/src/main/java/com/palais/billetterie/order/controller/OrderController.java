package com.palais.billetterie.order.controller;

import com.palais.billetterie.order.domain.Order;
import com.palais.billetterie.order.dto.OrderCreateRequest;
import com.palais.billetterie.order.dto.OrderUpdateRequest;
import com.palais.billetterie.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<Order> list() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(a -> a.equals("ROLE_ADMIN"));
        if (isAdmin) {
            return service.getAll();
        }
        String email = (String) auth.getPrincipal();
        return service.getAllForUserEmail(email);
    }

    @GetMapping("/{id}")
    public Order get(@PathVariable("id") UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(a -> a.equals("ROLE_ADMIN"));
        if (isAdmin) {
            return service.getById(id);
        }
        String email = (String) auth.getPrincipal();
        return service.getByIdForUserEmail(email, id);
    }

    @PostMapping
    public Order create(@RequestBody @Valid OrderCreateRequest req) {
        return service.create(req.getUserId(), req.getEventId(), req.getAmount());
    }

    @PutMapping("/{id}")
    public Order update(@PathVariable("id") UUID id, @RequestBody @Valid OrderUpdateRequest req) {
        return service.update(id, req.getAmount(), req.getStatus());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") UUID id) {
        service.delete(id);
    }
}